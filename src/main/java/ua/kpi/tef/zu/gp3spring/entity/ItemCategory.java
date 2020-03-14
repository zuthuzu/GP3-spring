package ua.kpi.tef.zu.gp3spring.entity;

/**
 * Created by Anton Domin on 2020-03-14
 */
public enum ItemCategory {
	COMPUTER ("category.computer"),
	PHONE ("category.phone"),
	ELECTRONICS("category.electronics"),
	APPLIANCE("category.appliance"),
	MECHANICAL("category.mechanical"),
	CLOTHING("category.clothing");

	private String value;

	ItemCategory(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
