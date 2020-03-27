package ua.kpi.tef.zu.gp3spring.entity.states;

import ua.kpi.tef.zu.gp3spring.entity.RoleType;

import java.util.Arrays;

/**
 * Created by Anton Domin on 2020-03-25
 */
@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class ReadyState extends AbstractState {
	public ReadyState() {
		setCurrentState(OrderStatus.READY);
		setNextState(OrderStatus.ARCHIVED);
		setRequiredRole(RoleType.ROLE_MANAGER);
		setAvailableFields(Arrays.asList("manager_comment"));
		setCancelable(false);
		setButtonText("order.action.delivered");
	}
}
