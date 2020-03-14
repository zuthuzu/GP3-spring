package ua.kpi.tef.zu.gp3spring.entity;

/**
 * Created by Anton Domin on 2020-03-14
 */
public enum OrderStatus {
	PENDING ("status.pending"),
	WORKING ("status.working"),
	READY("status.ready"),
	ARCHIVED("status.archived"),
	CANCELLED("status.cancelled");

	private String value;

	OrderStatus(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
