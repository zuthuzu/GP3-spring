package ua.kpi.tef.zu.gp3spring.entity.states;

import ua.kpi.tef.zu.gp3spring.entity.RoleType;

import java.util.Arrays;

/**
 * Created by Anton Domin on 2020-03-25
 */
@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class ArchivedState extends AbstractState {
	public ArchivedState() {
		setCurrentState(OrderStatus.ARCHIVED);
		setNextState(OrderStatus.ARCHIVED);
		setRequiredRole(RoleType.ROLE_USER);
		setRequiredFields(Arrays.asList("user_stars"));
		setAvailableFields(Arrays.asList("user_stars", "user_comment"));
		setCancelable(false);
		setButtonText("order.action.rate");
	}
}
