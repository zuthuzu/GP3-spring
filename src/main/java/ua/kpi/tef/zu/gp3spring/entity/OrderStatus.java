package ua.kpi.tef.zu.gp3spring.entity;

/**
 * Created by Anton Domin on 2020-03-14
 */
public enum OrderStatus {
	PENDING ("order.status.pending"),
	WORKING ("order.status.working"),
	READY("order.status.ready"),
	ARCHIVED("order.status.archived"),
	CANCELLED("order.status.cancelled");

	private String value;

	OrderStatus(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
