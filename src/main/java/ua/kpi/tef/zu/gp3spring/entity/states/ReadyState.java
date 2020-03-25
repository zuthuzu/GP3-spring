package ua.kpi.tef.zu.gp3spring.entity.states;

import ua.kpi.tef.zu.gp3spring.dto.OrderDTO;

/**
 * Created by Anton Domin on 2020-03-25
 */
public class ReadyState extends AbstractState {
	public ReadyState(OrderDTO order) {
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
