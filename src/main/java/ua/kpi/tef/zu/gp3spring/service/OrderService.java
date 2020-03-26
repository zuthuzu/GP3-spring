package ua.kpi.tef.zu.gp3spring.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.kpi.tef.zu.gp3spring.controller.RegistrationException;
import ua.kpi.tef.zu.gp3spring.dto.OrderDTO;
import ua.kpi.tef.zu.gp3spring.entity.RoleType;
import ua.kpi.tef.zu.gp3spring.entity.User;
import ua.kpi.tef.zu.gp3spring.entity.states.AbstractState;
import ua.kpi.tef.zu.gp3spring.entity.states.OrderStatus;
import ua.kpi.tef.zu.gp3spring.entity.WorkOrder;
import ua.kpi.tef.zu.gp3spring.entity.states.StateFactory;
import ua.kpi.tef.zu.gp3spring.repository.OrderRepo;

import java.time.LocalDate;
import java.util.*;

/**
 * Created by Anton Domin on 2020-03-22
 */
@Slf4j
@Service
public class OrderService {
	private final static String ACTION_PROCEED = "proceed";
	private final static String ACTION_CANCEL = "cancel";

	private OrderRepo orderRepo;

	@Autowired
	private UserService userService;

	@Autowired
	public OrderService(OrderRepo orderRepository) {
		this.orderRepo = orderRepository;
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

	public List<OrderDTO> getOrdersByMaster(String login) {
		return wrapWorkCollectionInDTO(orderRepo.findByMaster(login));
	}

	public List<OrderDTO> getOrdersByManager(String login) {
		return wrapWorkCollectionInDTO(orderRepo.findByManager(login));
	}

	public List<OrderDTO> getOrdersByAuthor(String login) {
		return wrapWorkCollectionInDTO(orderRepo.findByAuthor(login));
	}

	public List<OrderDTO> getActiveOrders() {
		List<OrderStatus> filter = Arrays.asList(OrderStatus.PENDING, OrderStatus.ACCEPTED, OrderStatus.WORKING, OrderStatus.READY);
		return wrapWorkCollectionInDTO(orderRepo.findByStatusIn(filter));
	}

	public List<OrderDTO> getArchivedOrders() {
		List<OrderStatus> filter = Arrays.asList(OrderStatus.ARCHIVED, OrderStatus.CANCELLED);
		return wrapWorkCollectionInDTO(orderRepo.findByStatusIn(filter));
	}

	public void saveNewOrder(OrderDTO order) throws RegistrationException {
		saveOrder(unwrapNewOrder(order));
	}



	/** Preparation and support for state change.<br />
	 * Verifies data retrieved from front end before passing it to the primary logic.
	 * @param modelOrder order the way it arrived from frontend
	 * @param initiator user who initiated the update
	 * @return success of the operation
	 */
	public boolean updateOrder(OrderDTO modelOrder, User initiator) {
		if (!modelOrder.getAction().equals(ACTION_PROCEED) && !modelOrder.getAction().equals(ACTION_CANCEL)) {
			log.error("Illegal action in update request: " + modelOrder);
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
			log.error("Illegal cancel attempt in update request: " + modelOrder);
			return false;
		}
		if (initiator.getRole() != state.getRequiredRole()) {
			log.error("Illegal user role in update request: " + modelOrder);
			return false;
		}
		if (!verifyRequiredFields(modelOrder, proceed ? state.getRequiredFields() : state.getPreCancelFields())) {
			log.error("Incomplete data in update request: " + modelOrder);
			return false;
		}

		WorkOrder compositeOrder = assembleOrder(dbOrder, modelOrder, initiator, proceed);

		try {
			saveOrder(compositeOrder);
		} catch (RegistrationException e) {
			return false;
		}

		return true;
	}

	private boolean verifyRequiredFields(OrderDTO modelOrder, List<String> fields) {
		for (String field : fields) {
			switch (field) {
				case "price":
					if (modelOrder.getPrice() == 0) return false;
					break;
				case "manager_comment":
					if (isEmptyOrNull(modelOrder.getManagerComment())) return false;
					break;
				case "master_comment":
					if (isEmptyOrNull(modelOrder.getMasterComment())) return false;
					break;
			}
		}
		return true;
	}

	/** Primary logic that invokes the entire order state mechanism.<br />
	 *  Carefully applies front end data onto DB data where it is necessitated by the current state.
	 * @param dbOrder order the way it's currently present in DB
	 * @param modelOrder order the way it arrived from frontend
	 * @param initiator user who initiated the update
	 * @return an entity ready for updating into DB
	 */
	private WorkOrder assembleOrder(OrderDTO dbOrder, OrderDTO modelOrder, User initiator, boolean proceed) {
		AbstractState state = dbOrder.getLiveState();
		List<String> availableFields = state.getAvailableFields();

		return WorkOrder.builder()
				.id(dbOrder.getId())
				.creationDate(dbOrder.getActualCreationDate())
				.author(dbOrder.getAuthorLogin())
				.manager(state.getRequiredRole() == RoleType.ROLE_MANAGER ? initiator.getLogin() : dbOrder.getManagerLogin())
				.master(state.getRequiredRole() == RoleType.ROLE_MASTER ? initiator.getLogin() : dbOrder.getMasterLogin())
				.status(proceed ? state.getNextState() : OrderStatus.CANCELLED)
				.category(availableFields.contains("category") ? modelOrder.getActualCategory() : dbOrder.getActualCategory())
				.item(availableFields.contains("item") ? modelOrder.getItem() : dbOrder.getItem())
				.complaint(availableFields.contains("complaint") ? modelOrder.getComplaint() : dbOrder.getComplaint())
				.price(availableFields.contains("price") ? modelOrder.getPrice() : dbOrder.getPrice())
				.managerComment(availableFields.contains("manager_comment") ? modelOrder.getManagerComment() : dbOrder.getManagerComment())
				.masterComment(availableFields.contains("master_comment") ? modelOrder.getMasterComment() : dbOrder.getMasterComment())
				.build();
	}

	private void saveOrder(WorkOrder order) throws RegistrationException {
		try {
			orderRepo.save(order);
			log.info(order.getStatus() == OrderStatus.PENDING ? "New order created: " : "Order updated: " + order);
		} catch (Exception e) {
			log.error("Couldn't save a new order", e);
			throw new RegistrationException(e);
		}
	}

	private List<OrderDTO> wrapWorkCollectionInDTO(List<WorkOrder> entities) {
		//TODO DB access optimization: read all involved users in one query, to avoid 3 separate reads per order
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

	/** As far as I could tell, there's no native way to check for it in java.<br /><br />
	 * Apache Commons has StringUtils.isEmpty(value), but I don't want to include it here.*/
	private boolean isEmptyOrNull(String value) {
		return value == null || value.isEmpty();
	}
}
