package ua.kpi.tef.zu.gp3spring.entity.states;

/**
 * Created by Anton Domin on 2020-03-14
 */
public enum OrderStatus {
	PENDING ("order.status.pending", false),
	ACCEPTED ("order.status.accepted", false),
	WORKING ("order.status.working", false),
	READY("order.status.ready", false),
	ARCHIVED("order.status.archived", true),
	CANCELLED("order.status.cancelled", true);

	private String value;
	private boolean archived;

	OrderStatus(String value, boolean archived) {
		this.value = value;
		this.archived = archived;
	}

	@Override
	public String toString() {
		return value;
	}

	public boolean isArchived() {
		return archived;
	}
}
