package ua.kpi.tef.zu.gp3spring.entity.states;

import ua.kpi.tef.zu.gp3spring.dto.OrderDTO;
import ua.kpi.tef.zu.gp3spring.entity.RoleType;

import java.util.Arrays;

/**
 * Created by Anton Domin on 2020-03-25
 */
@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
public class WorkingState extends AbstractState {
	public WorkingState() {
		setCurrentState(OrderStatus.WORKING);
		setNextState(OrderStatus.READY);
		setRequiredRole(RoleType.ROLE_MASTER);
		//setRequiredFields(); //no required fields at this state
		setAvailableFields(Arrays.asList("master_comment"));
		setCancelable(true);
		setPreCancelFields(Arrays.asList("master_comment"));
		setButtonText("order.action.done");
	}

	@Override
	public void applyAvailableFields(OrderDTO to, OrderDTO from) {
		to.setMasterComment(!isEmptyOrNull(from.getMasterComment()) ? from.getMasterComment() : to.getMasterComment());
	}
}
