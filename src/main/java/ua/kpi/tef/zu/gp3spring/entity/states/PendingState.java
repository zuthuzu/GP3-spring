package ua.kpi.tef.zu.gp3spring.entity.states;

import ua.kpi.tef.zu.gp3spring.dto.OrderDTO;
import ua.kpi.tef.zu.gp3spring.entity.RoleType;

import java.util.Arrays;

/**
 * Created by Anton Domin on 2020-03-25
 */
@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class PendingState extends AbstractState {
	public PendingState(OrderDTO order) {
		super(order);
		setNextState(OrderStatus.ACCEPTED);

		setRequiredRole(RoleType.ROLE_MANAGER);
		setRequiredFields(Arrays.asList("price"));
		setAvailableFields(Arrays.asList("status", "manager", "manager_comment", "price"));
		setCancelable(true);
		setPreCancelFields(Arrays.asList("manager_comment"));
		setButtonText("order.action.accept");
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
