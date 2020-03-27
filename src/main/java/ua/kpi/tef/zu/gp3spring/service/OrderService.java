package ua.kpi.tef.zu.gp3spring.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.tef.zu.gp3spring.controller.RegistrationException;
import ua.kpi.tef.zu.gp3spring.dto.OrderDTO;
import ua.kpi.tef.zu.gp3spring.entity.ArchiveOrder;
import ua.kpi.tef.zu.gp3spring.entity.RoleType;
import ua.kpi.tef.zu.gp3spring.entity.User;
import ua.kpi.tef.zu.gp3spring.entity.states.AbstractState;
import ua.kpi.tef.zu.gp3spring.entity.states.OrderStatus;
import ua.kpi.tef.zu.gp3spring.entity.WorkOrder;
import ua.kpi.tef.zu.gp3spring.entity.states.StateFactory;
import ua.kpi.tef.zu.gp3spring.repository.ArchiveRepo;
import ua.kpi.tef.zu.gp3spring.repository.OrderRepo;

import java.time.LocalDate;
import java.util.*;

/**
 * Created by Anton Domin on 2020-03-22
 */
@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
@Slf4j
@Service
public class OrderService {
	private final static String ACTION_PROCEED = "proceed";
	private final static String ACTION_CANCEL = "cancel";

	private OrderRepo orderRepo;
	private ArchiveRepo archiveRepo;

	@Autowired
	private UserService userService;

	@Autowired
	public OrderService(OrderRepo orderRepository, ArchiveRepo archiveRepository) {
		this.orderRepo = orderRepository;
		this.archiveRepo = archiveRepository;
	}

	/**
	 * Returns so called "active orders", i.e. requiring primary attention from the user.
	 * <ul>
	 * <li>For masters it's orders they're working on (status.WORKING, master==themselves)
	 * <li>For managers it's orders they have to deal with (PENDING with no manager or READY with manager==themselves)
	 * <li>For users it's their orders that are still being repaired (any non-archived with author==themselves)
	 * </ul>
	 *
	 * @param initiator user who initiated the request
	 * @return list of the orders that satisfy the condition
	 */
	public List<OrderDTO> getActiveOrders(User initiator) {
		List<OrderStatus> filter;
		if (initiator.getRole() == RoleType.ROLE_MASTER) {
			filter = Arrays.asList(OrderStatus.ACCEPTED, OrderStatus.WORKING);
			return wrapWorkCollectionInDTO(orderRepo.findByMasterAndStatusIn(initiator.getLogin(), filter));

		} else if (initiator.getRole() == RoleType.ROLE_MANAGER) {
			filter = Arrays.asList(OrderStatus.PENDING);
			List<WorkOrder> orders = orderRepo.findByStatusIn(filter);

			filter = Arrays.asList(OrderStatus.READY);
			orders.addAll(orderRepo.findByManagerAndStatusIn(initiator.getLogin(), filter));

			return wrapWorkCollectionInDTO(orders);

		} else {
			filter = Arrays.asList(OrderStatus.PENDING, OrderStatus.ACCEPTED, OrderStatus.WORKING, OrderStatus.READY);
			return wrapWorkCollectionInDTO(orderRepo.findByAuthorAndStatusIn(initiator.getLogin(), filter));
		}
	}

	/**
	 * Returns so called "secondary orders", i.e. orders that user should care about less.
	 * <ul>
	 * <li>For masters it's accepted orders that still haven't been taken (status.ACCEPTED)
	 * <li>For managers it's their orders that are still being worked on (ACCEPTED or WORKING, manager==themselves)
	 * <li>For users it's their long term history (archived and cancelled)
	 * </ul>
	 *
	 * @param initiator user who initiated the request
	 * @return list of the orders that satisfy the condition
	 */
	public List<OrderDTO> getSecondaryOrders(User initiator) {
		List<OrderStatus> filter;
		if (initiator.getRole() == RoleType.ROLE_MASTER) {
			filter = Arrays.asList(OrderStatus.ACCEPTED);
			return wrapWorkCollectionInDTO(orderRepo.findByStatusIn(filter));
		} else if (initiator.getRole() == RoleType.ROLE_MANAGER) {
			filter = Arrays.asList(OrderStatus.ACCEPTED, OrderStatus.WORKING);
			return wrapWorkCollectionInDTO(orderRepo.findByManagerAndStatusIn(initiator.getLogin(), filter));
		} else {
			return wrapWorkCollectionInDTO(archiveRepo.findByAuthor(initiator.getLogin()));
		}
	}

	/**
	 * Primary logic that invokes the order state mechanism.<br />
	 * Preparation and support for state change.<br />
	 * Verifies data retrieved from front end before passing it to the primary logic.
	 *
	 * @param modelOrder order the way it arrived from frontend
	 * @param initiator  user who initiated the update
	 * @return success of the operation
	 */
	public boolean updateOrder(OrderDTO modelOrder, User initiator) {
		if (!modelOrder.getAction().equals(ACTION_PROCEED) && !modelOrder.getAction().equals(ACTION_CANCEL)) {
			log.error("Illegal action in update request: " + modelOrder.toStringSkipEmpty());
			return false;
		}
		boolean proceed = modelOrder.getAction().equals(ACTION_PROCEED); //false == cancel

		OrderDTO dbOrder;
		try {
			dbOrder = getOrderById(String.valueOf(modelOrder.getId()));
		} catch (IllegalArgumentException e) {
			log.error(e.getMessage());
			return false;
		}

		/*modelOrder that we obtained from frontend has only the barest minimum of fields
		Meaning no live state assigned, and no status at all, in fact
		However, since change hasn't occurred yet, dbOrder should have the same state.
		This means we have to postpone these checks until after we've performed a DB read*/
		AbstractState state = dbOrder.getLiveState();
		if (!proceed && !state.isCancelable()) {
			log.error("Illegal cancel attempt in update request: " + modelOrder.toStringSkipEmpty());
			return false;
		}
		if (initiator.getRole() != state.getRequiredRole()) {
			log.error("Illegal user role in update request: " + modelOrder.toStringSkipEmpty());
			return false;
		}
		if (!verifyRequiredFields(modelOrder, proceed ? state.getRequiredFields() : state.getPreCancelFields())) {
			return false;
		}

		modelOrder.setActualStatus(proceed ? state.getNextState() : OrderStatus.CANCELLED); //smuggling a parameter in DTO

		OrderDTO preparedOrder = assembleOrder(dbOrder, modelOrder, initiator);

		try {
			if (state.moveToArchive(proceed)) {
				archiveOrder(unwrapFullOrder(preparedOrder));
			} else {
				saveOrder(unwrapFullOrder(preparedOrder));
			}
		} catch (RegistrationException e) {
			return false;
		}

		return true;
	}

	private boolean verifyRequiredFields(OrderDTO modelOrder, List<String> fields) {
		for (String field : fields) {
			switch (field) {
				case "price":
					if (modelOrder.getPrice() == 0) {
						log.error("Incomplete data: missing price in update request: " + modelOrder.toStringSkipEmpty());
						return false;
					}
					break;
				case "manager_comment":
					if (isEmptyOrNull(modelOrder.getManagerComment())) {
						log.error("Incomplete data: missing manager comment in update request: " + modelOrder.toStringSkipEmpty());
						return false;
					}
					break;
				case "master_comment":
					if (isEmptyOrNull(modelOrder.getMasterComment())) {
						log.error("Incomplete data: missing master comment in update request: " + modelOrder.toStringSkipEmpty());
						return false;
					}
					break;
				case "user_stars":
					if (modelOrder.getUserStars() == 0) {
						log.error("Incomplete data: missing user rating in update request: " + modelOrder.toStringSkipEmpty());
						return false;
					}
					break;
			}
		}
		return true;
	}

	/**
	 * Carefully applies front end data onto DB data where it is necessitated by the current state.
	 *
	 * @param dbOrder    order the way it's currently present in DB
	 * @param modelOrder order the way it arrived from frontend
	 * @param initiator  user who initiated the update
	 * @return an entity ready for updating into DB
	 */
	private OrderDTO assembleOrder(OrderDTO dbOrder, OrderDTO modelOrder, User initiator) {
		AbstractState state = dbOrder.getLiveState();
		List<String> availableFields = state.getAvailableFields();

		return OrderDTO.builder()
				.id(dbOrder.getId())
				.actualCreationDate(dbOrder.getActualCreationDate())
				.authorLogin(dbOrder.getAuthorLogin())
				.managerLogin((state.getRequiredRole() == RoleType.ROLE_MANAGER && isEmptyOrNull(dbOrder.getManagerLogin()))
						? initiator.getLogin() : dbOrder.getManagerLogin()) //first authorised initiator gets recorded
				.masterLogin((state.getRequiredRole() == RoleType.ROLE_MASTER && isEmptyOrNull(dbOrder.getMasterLogin()))
						? initiator.getLogin() : dbOrder.getMasterLogin()) //first authorised initiator gets recorded
				.actualStatus(modelOrder.getActualStatus()) //by now it's set as either state.getNextState() or CANCELLED
				.actualCategory((availableFields.contains("category") && modelOrder.getActualCategory() != null)
						? modelOrder.getActualCategory() : dbOrder.getActualCategory()) //this field doesn't get checked well enough previously
				.item(availableFields.contains("item") ? modelOrder.getItem() : dbOrder.getItem())
				.complaint(availableFields.contains("complaint") ? modelOrder.getComplaint() : dbOrder.getComplaint())
				.price(availableFields.contains("price") ? modelOrder.getPrice() : dbOrder.getPrice())
				.managerComment(availableFields.contains("manager_comment") ? modelOrder.getManagerComment() : dbOrder.getManagerComment())
				.masterComment(availableFields.contains("master_comment") ? modelOrder.getMasterComment() : dbOrder.getMasterComment())
				.userComment(availableFields.contains("user_comment") ? modelOrder.getUserComment() : dbOrder.getUserComment())
				.userStars(availableFields.contains("user_stars") ? modelOrder.getUserStars() : dbOrder.getUserStars())
				.build();
	}

	public OrderDTO getOrderById(String id) throws IllegalArgumentException {
		long realID;
		try {
			realID = Long.parseLong(id);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Can't convert ID " + id + " to a number.");
		}

		WorkOrder order = orderRepo.findById(realID).orElseThrow(() -> new IllegalArgumentException("Can't find order with ID " + id));
		return wrapOrderInDTO(order);
	}

	public void saveNewOrder(OrderDTO order) throws RegistrationException {
		saveOrder(unwrapNewOrder(order));
	}

	public void saveOrder(WorkOrder order) throws RegistrationException {
		try {
			orderRepo.save(order);
			log.info(order.getStatus() == OrderStatus.PENDING ? "New order created: " : "Order updated: " + order);
		} catch (Exception e) {
			log.error("Couldn't save a new order", e);
			throw new RegistrationException(e);
		}
	}

	@Transactional
	public void archiveOrder(WorkOrder order) {
		log.info("Archive attempt: " + order);
	}

	private List<OrderDTO> wrapWorkCollectionInDTO(List<WorkOrder> entities) {
		//TODO DB access optimization: read all involved users in one query to avoid 3 separate reads per order
		List<OrderDTO> result = new ArrayList<>();
		for (WorkOrder order : entities) {
			result.add(wrapOrderInDTO(order));
		}
		return result;
	}

	private OrderDTO wrapOrderInDTO(WorkOrder order) {
		OrderDTO result = OrderDTO.builder()
				.id(order.getId())
				.actualCreationDate(order.getCreationDate())
				.author(userService.getUsernameByLogin(order.getAuthor()))
				.authorLogin(order.getAuthor())
				.manager(userService.getUsernameByLogin(order.getManager()))
				.managerLogin(order.getManager())
				.master(userService.getUsernameByLogin(order.getMaster()))
				.masterLogin(order.getMaster())
				.actualCategory(order.getCategory())
				.item(order.getItem())
				.complaint(order.getComplaint())
				.actualStatus(order.getStatus())
				.price(order.getPrice())
				.managerComment(order.getManagerComment())
				.masterComment(order.getMasterComment())
				.build();

		StateFactory.setState(result);
		return result;
	}

	private WorkOrder unwrapNewOrder(OrderDTO order) {
		return WorkOrder.builder()
				.author(order.getAuthor())
				.category(order.getActualCategory())
				.item(order.getItem())
				.complaint(order.getComplaint())
				.status(OrderStatus.PENDING)
				.creationDate(LocalDate.now())
				.build();
	}

	private WorkOrder unwrapFullOrder(OrderDTO order) {
		return WorkOrder.builder()
				.id(order.getId())
				.creationDate(order.getActualCreationDate())
				.author(order.getAuthorLogin())
				.manager(order.getManagerLogin())
				.master(order.getMasterLogin())
				.status(order.getActualStatus())
				.category(order.getActualCategory())
				.item(order.getItem())
				.complaint(order.getComplaint())
				.price(order.getPrice())
				.managerComment(order.getManagerComment())
				.masterComment(order.getMasterComment())
				.build();
	}

	/**
	 * As far as I can tell, there's no native way to check for it in java.<br /><br />
	 * Apache Commons has StringUtils.isEmpty(value), but I don't want to include it here.
	 */
	private boolean isEmptyOrNull(String value) {
		return value == null || value.isEmpty();
	}
}
