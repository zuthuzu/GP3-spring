package ua.kpi.tef.zu.gp3spring.entity.states;

import lombok.Getter;
import lombok.Setter;
import ua.kpi.tef.zu.gp3spring.entity.RoleType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anton Domin on 2020-03-25
 */
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

	/**
	 * @param proceed direction of state change (true = to next state, false = cancel the order)
	 * @return whether we need to move the order to archive during this state change
	 */
	public boolean moveToArchive(boolean proceed) {
		if (!proceed && isCancelable) return true;
		return proceed && nextState.isArchived() && !currentState.isArchived();
	}
}
