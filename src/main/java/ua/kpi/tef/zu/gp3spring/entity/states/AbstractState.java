package ua.kpi.tef.zu.gp3spring.entity.states;

import lombok.Getter;
import lombok.Setter;
import ua.kpi.tef.zu.gp3spring.dto.OrderDTO;
import ua.kpi.tef.zu.gp3spring.entity.RoleType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anton Domin on 2020-03-25
 */
@Getter
@Setter
public abstract class AbstractState {
	private OrderDTO order;
	private OrderStatus nextState;

	private RoleType requiredRole; //who can call proceed() from here onward
	private List<String> requiredFields = new ArrayList<>(); //what he must fill before proceeding
	private List<String> availableFields = new ArrayList<>(); //what he can change before proceeding

	private boolean isCancelable;
	private List<String> preCancelFields = new ArrayList<>(); //what he must fill before cancelling

	private String buttonText;

	public AbstractState(OrderDTO order) {
		this.order = order;
	}

	public abstract AbstractState proceed();
	public abstract AbstractState cancel();
}
