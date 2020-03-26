package ua.kpi.tef.zu.gp3spring.entity.states;

import ua.kpi.tef.zu.gp3spring.entity.RoleType;

import java.util.Arrays;

/**
 * Created by Anton Domin on 2020-03-25
 */
@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class PendingState extends AbstractState {
	public PendingState() {
		setNextState(OrderStatus.ACCEPTED);

		setRequiredRole(RoleType.ROLE_MANAGER);
		setRequiredFields(Arrays.asList("price"));
		setAvailableFields(Arrays.asList("category", "item", "manager_comment", "price"));
		setCancelable(true);
		setPreCancelFields(Arrays.asList("manager_comment"));
		setButtonText("order.action.accept");
	}
}
