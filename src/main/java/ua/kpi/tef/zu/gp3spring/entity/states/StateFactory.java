package ua.kpi.tef.zu.gp3spring.entity.states;

import ua.kpi.tef.zu.gp3spring.dto.OrderDTO;

/**
 * Created by Anton Domin on 2020-03-25
 */
public class StateFactory {
	public static AbstractState getState(OrderDTO order) {
		switch (order.getActualStatus()) {
			case PENDING:
				return new PendingState();
			case ACCEPTED:
				return new AcceptedState();
			case WORKING:
				return new WorkingState();
			case READY:
				return new ReadyState();
			case ARCHIVED:
				return new ArchivedState();
			default:
				return new CancelledState(); //technically it's case CANCELLED
		}
	}

	public static void setState(OrderDTO order) {
		order.setLiveState(getState(order));
	}
}
