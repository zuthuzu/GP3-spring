package ua.kpi.tef.zu.gp3spring.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.kpi.tef.zu.gp3spring.controller.DatabaseException;
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
	private final OrderRepo orderRepo;
	private final ArchiveRepo archiveRepo;
	private final UserService userService;
	private final TransactionService transactions;

	@Autowired
	public OrderService(OrderRepo orderRepository, ArchiveRepo archiveRepository,
						UserService userService, TransactionService transactionService) {
		this.orderRepo = orderRepository;
		this.archiveRepo = archiveRepository;
		this.userService = userService;
		this.transactions = transactionService;
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
	 * Verifies data retrieved from front end, then passes it to the primary logic.
	 *
	 * @param modelOrder order the way it arrived from frontend, + initiator (user who calls it)
	 */
	public void updateOrder(OrderDTO modelOrder) throws DatabaseException, IllegalArgumentException {
		OrderDTO dbOrder = getOrderById(modelOrder.getId());
		AbstractState state = dbOrder.getLiveState();
		state.verifyRequest(modelOrder);
		state.processOrder(this, state.assembleOrder(dbOrder, modelOrder));
	}

	public OrderDTO getOrderById(String id) throws IllegalArgumentException {
		long realID;
		try {
			realID = Long.parseLong(id);
		} catch (Exception e) {
			throw new IllegalArgumentException("Can't convert ID " + id + " to a number.");
		}
		return getOrderById(realID);
	}

	public OrderDTO getOrderById(long id) throws IllegalArgumentException {
		WorkOrder order = orderRepo.findById(id).orElseThrow(() ->
				new IllegalArgumentException("Can't find order with ID " + id));

		return wrapOrderInDTO(order, getUserCache(Arrays.asList(order)));
	}

	public void archiveOrder(OrderDTO order) throws DatabaseException {
		transactions.archiveOrder(unwrapFullOrder(order));
		log.info("Order archived: " + order.toStringSkipEmpty());
	}

	public void saveNewOrder(OrderDTO order) throws DatabaseException {
		saveOrder(unwrapNewOrder(order));
		log.info("New order created: " + order.toStringSkipEmpty());
	}

	public void saveExistingOrder(OrderDTO order) throws DatabaseException {
		saveOrder(unwrapFullOrder(order));
		log.info((order.getActualStatus().isArchived() ? "Archived order updated: " : "Order updated: ") +
				order.toStringSkipEmpty());
	}

	private void saveOrder(WorkOrder order) throws DatabaseException {
		OrderStatus status = order.getStatus();
		if (status.isArchived()) {
			ArchiveOrder archiveOrder;

			try {
				archiveOrder = (ArchiveOrder) order;
			} catch (ClassCastException e) {
				throw new DatabaseException("Bad order downcast: " + order);
			}

			try {
				archiveRepo.save(archiveOrder);
			} catch (Exception e) {
				throw new DatabaseException("Couldn't save an order: " + order, e);
			}
		} else {
			try {
				orderRepo.save(order);
			} catch (Exception e) {
				throw new DatabaseException("Couldn't save an order: " + order, e);
			}
		}
	}

	private List<OrderDTO> wrapWorkCollectionInDTO(List<? extends WorkOrder> entities) {
		Map<String, String> userCache = getUserCache(entities);
		List<OrderDTO> result = new ArrayList<>();
		for (WorkOrder order : entities) {
			result.add(wrapOrderInDTO(order, userCache));
		}
		return result;
	}

	/**
	 * Without this cache wrapWorkCollectionInDTO would've performed 3 separate DB reads for each order
	 */
	private Map<String, String> getUserCache(List<? extends WorkOrder> entities) {
		Map<String, String> userCache = new HashMap<>();
		for (WorkOrder order : entities) {
			userCache.put(order.getAuthor(), "");
			userCache.put(order.getManager(), "");
			userCache.put(order.getMaster(), "");
		}

		List<User> userList = userService.loadUsersByLoginCollection(userCache.keySet());
		userList.forEach(u -> userCache.put(u.getLogin(), u.getName()));
		return userCache;
	}

	private OrderDTO wrapOrderInDTO(WorkOrder order, Map<String, String> userCache) {
		OrderDTO result = OrderDTO.builder()
				.id(order.getId())
				.actualCreationDate(order.getCreationDate())
				.author(userCache.get(order.getAuthor()))
				.authorLogin(order.getAuthor())
				.manager(userCache.get(order.getManager()))
				.managerLogin(order.getManager())
				.master(userCache.get(order.getMaster()))
				.masterLogin(order.getMaster())
				.actualCategory(order.getCategory())
				.item(order.getItem())
				.complaint(order.getComplaint())
				.actualStatus(order.getStatus())
				.isArchived(order.getStatus().isArchived())
				.price(order.getPrice())
				.managerComment(order.getManagerComment())
				.masterComment(order.getMasterComment())
				.build();

		StateFactory.setState(result);
		if (result.isArchived()) {
			try {
				ArchiveOrder archiveOrder = (ArchiveOrder) order;
				result.setUserComment(archiveOrder.getUserComment());
				result.setUserStars(archiveOrder.getUserStars());
			} catch (ClassCastException e) {
				log.error("Bad order downcast: " + order);
			}
		}
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
		WorkOrder result;
		if (order.getActualStatus().isArchived()) {
			result = new ArchiveOrder(order.getUserComment(), order.getUserStars());
		} else {
			result = new WorkOrder();
		}
		result.setId(order.getId());
		result.setCreationDate(order.getActualCreationDate());
		result.setAuthor(order.getAuthorLogin());
		result.setManager(order.getManagerLogin());
		result.setMaster(order.getMasterLogin());
		result.setStatus(order.getActualStatus());
		result.setCategory(order.getActualCategory());
		result.setItem(order.getItem());
		result.setComplaint(order.getComplaint());
		result.setPrice(order.getPrice());
		result.setManagerComment(order.getManagerComment());
		result.setMasterComment(order.getMasterComment());
		return result;
	}
}
