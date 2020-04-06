package ua.kpi.tef.zu.gp3spring.entity.states;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ua.kpi.tef.zu.gp3spring.controller.DatabaseException;
import ua.kpi.tef.zu.gp3spring.dto.OrderDTO;
import ua.kpi.tef.zu.gp3spring.entity.RoleType;
import ua.kpi.tef.zu.gp3spring.entity.User;
import ua.kpi.tef.zu.gp3spring.service.OrderService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anton Domin on 2020-03-25
 */
@Slf4j
@Getter
@Setter
public abstract class AbstractState {
	private OrderStatus currentState;
	private OrderStatus nextState;

	private RoleType requiredRole; //who can call initiate a change from this state to another
	private List<String> requiredFields = new ArrayList<>(); //what he MUST fill before proceeding
	private List<String> availableFields = new ArrayList<>(); //what he CAN change before proceeding

	private boolean isCancelable;
	private List<String> preCancelFields = new ArrayList<>(); //what he MUST fill before cancelling

	private String buttonText;

	public void verifyRequest(OrderDTO from) throws IllegalArgumentException {
		if (!from.getAction().equals(OrderDTO.ACTION_PROCEED) && !from.getAction().equals(OrderDTO.ACTION_CANCEL)) {
			throw new IllegalArgumentException("Illegal action in update request: " + from.toStringSkipEmpty());
		}
		if (!from.proceed() && !isCancelable()) {
			throw new IllegalArgumentException("Illegal cancel attempt in update request: " + from.toStringSkipEmpty());
		}
		if (from.getInitiator().getRole() != getRequiredRole()) {
			throw new IllegalArgumentException("Illegal user role in update request: " + from.toStringSkipEmpty());
		}
		verifyRequiredFields(from);
	}


	public void verifyRequiredFields(OrderDTO from) throws IllegalArgumentException {
		for (String field : from.proceed() ? requiredFields : preCancelFields) {
			switch (field) {
				case "price":
					if (from.getPrice() == 0) {
						throw new IllegalArgumentException("Incomplete data: missing price in update request: "
								+ from.toStringSkipEmpty());
					}
					break;
				case "manager_comment":
					if (isEmptyOrNull(from.getManagerComment())) {
						throw new IllegalArgumentException("Incomplete data: missing manager comment in update request: "
								+ from.toStringSkipEmpty());
					}
					break;
				case "master_comment":
					if (isEmptyOrNull(from.getMasterComment())) {
						throw new IllegalArgumentException("Incomplete data: missing master comment in update request: "
								+ from.toStringSkipEmpty());
					}
					break;
				case "user_stars":
					if (from.getUserStars() <= 0 || from.getUserStars() > 5) {
						throw new IllegalArgumentException("Incomplete data: missing user rating in update request: "
								+ from.toStringSkipEmpty());
					}
					break;
			}
		}
	}

	public void processOrder(OrderService service, OrderDTO order) throws DatabaseException {
		if (moveToArchive(order.proceed())) {
			service.archiveOrder(order);
		} else {
			service.saveExistingOrder(order);
		}
	}

	/**
	 * Carefully applies front end data onto DB data where it is necessitated by the current state.
	 *
	 * @param dbOrder    order the way it's currently present in DB
	 * @param modelOrder order the way it arrived from frontend
	 * @return an entity ready for updating into DB
	 */
	public OrderDTO assembleOrder(OrderDTO dbOrder, OrderDTO modelOrder) {
		dbOrder.setManagerLogin((requiredRole == RoleType.ROLE_MANAGER && isEmptyOrNull(dbOrder.getManagerLogin()))
				? modelOrder.getInitiator().getLogin() : dbOrder.getManagerLogin()); //first authorised initiator gets recorded
		dbOrder.setMasterLogin((requiredRole == RoleType.ROLE_MASTER && isEmptyOrNull(dbOrder.getMasterLogin()))
				? modelOrder.getInitiator().getLogin() : dbOrder.getMasterLogin()); //first authorised initiator gets recorded

		dbOrder.setAction(modelOrder.getAction());
		dbOrder.setInitiator(modelOrder.getInitiator());
		dbOrder.setActualStatus(dbOrder.proceed() ? nextState : OrderStatus.CANCELLED);

		applyAvailableFields(dbOrder, modelOrder);
		return dbOrder;
	}

	public abstract void applyAvailableFields(OrderDTO to, OrderDTO from);

	/**
	 * @param proceed direction of state change (true = to next state, false = cancel the order)
	 * @return whether we need to move the order to archive during this state change
	 */
	public boolean moveToArchive(boolean proceed) {
		if (!proceed && isCancelable) return true;
		return proceed && nextState.isArchived() && !currentState.isArchived();
	}

	/**
	 * As far as I can tell, there's no native way to check for it in java.<br /><br />
	 * Apache Commons has StringUtils.isEmpty(value), but I don't want to include it here.
	 */
	public boolean isEmptyOrNull(String value) {
		return value == null || value.isEmpty();
	}
}
