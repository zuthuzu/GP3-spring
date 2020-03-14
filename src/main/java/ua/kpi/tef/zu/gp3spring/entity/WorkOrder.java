package ua.kpi.tef.zu.gp3spring.entity;

import lombok.*;

import javax.persistence.*;

/**
 * Created by Anton Domin on 2020-03-14
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString

@Entity
@Table(name = "orders")
public class WorkOrder {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(nullable = false)
	private String author;

	@Column(nullable = false)
	private String manager;

	@Column(nullable = false)
	private String master;

	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	@Column(name = "category")
	@Enumerated(EnumType.STRING)
	private ItemCategory category;

	@Column(nullable = false)
	private String item;

	@Column(nullable = false)
	private String complaint;

	private int price;

	@Column(name = "manager_comment")
	private String managerComment;

	@Column(name = "master_comment")
	private String masterComment;
}
