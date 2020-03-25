package ua.kpi.tef.zu.gp3spring.entity.states;

import ua.kpi.tef.zu.gp3spring.dto.OrderDTO;

/**
 * Created by Anton Domin on 2020-03-25
 */
public class CancelledState extends AbstractState {
	public CancelledState(OrderDTO order) {
		super(order);
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
