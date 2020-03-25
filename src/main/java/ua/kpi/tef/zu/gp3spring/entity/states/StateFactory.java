package ua.kpi.tef.zu.gp3spring.entity.states;

import ua.kpi.tef.zu.gp3spring.dto.OrderDTO;

/**
 * Created by Anton Domin on 2020-03-25
 */
public class StateFactory {
	public static AbstractState getState(OrderDTO order, OrderStatus status) {
		switch (status) {
			case PENDING:
				return new PendingState(order);
			case ACCEPTED:
				return new AcceptedState(order);
			case WORKING:
				return new WorkingState(order);
			case READY:
				return new ReadyState(order);
			case ARCHIVED:
				return new ArchivedState(order);
			default:
				return new CancelledState(order); //technically it's case CANCELLED
		}
	}
}
