package ua.kpi.tef.zu.gp3spring.entity.states;

import ua.kpi.tef.zu.gp3spring.dto.OrderDTO;
import ua.kpi.tef.zu.gp3spring.entity.RoleType;

import java.util.Arrays;

/**
 * Created by Anton Domin on 2020-03-25
 */
@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class CancelledState extends AbstractState {
	public CancelledState() {
		setCurrentState(OrderStatus.CANCELLED);
		setNextState(OrderStatus.CANCELLED);
		setRequiredRole(RoleType.ROLE_USER);
		setRequiredFields(Arrays.asList("user_stars"));
		setAvailableFields(Arrays.asList("user_stars", "user_comment"));
		setCancelable(false);
		setButtonText("order.action.rate");
	}

	@Override
	public void applyAvailableFields(OrderDTO to, OrderDTO from) {
		to.setUserComment(!isEmptyOrNull(from.getUserComment()) ? from.getUserComment() : to.getUserComment());
		to.setUserStars(from.getUserStars());
	}
}
