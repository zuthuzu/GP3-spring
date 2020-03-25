package ua.kpi.tef.zu.gp3spring.entity.states;

import ua.kpi.tef.zu.gp3spring.dto.OrderDTO;
import ua.kpi.tef.zu.gp3spring.entity.RoleType;

import java.util.Arrays;

/**
 * Created by Anton Domin on 2020-03-25
 */
@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class AcceptedState extends AbstractState {
	public AcceptedState(OrderDTO order) {
		super(order);
		setNextState(OrderStatus.WORKING);

		setRequiredRole(RoleType.ROLE_MASTER);
		//setRequiredFields(); //no required fields at this state
		setAvailableFields(Arrays.asList("status", "master", "master_comment"));
		setCancelable(true);
		setPreCancelFields(Arrays.asList("manager_comment"));
		setButtonText("order.action.take");
	}

	@Override
	public AbstractState proceed() {
		return StateFactory.getState(getOrder(), getNextState());
	}

	@Override
	public AbstractState cancel() {
		return StateFactory.getState(getOrder(), OrderStatus.CANCELLED);
	}
}
