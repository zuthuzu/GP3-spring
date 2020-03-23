package ua.kpi.tef.zu.gp3spring.dto;

import lombok.*;
import ua.kpi.tef.zu.gp3spring.entity.ItemCategory;
import ua.kpi.tef.zu.gp3spring.entity.OrderStatus;

import java.time.LocalDate;

/**
 * Created by Anton Domin on 2020-03-22
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class OrderDTO {
	private long id;
	private String creationDate;
	private LocalDate actualCreationDate;
	private String author;
	private String manager;
	private String master;
	private String category;
	private ItemCategory actualCategory;
	private String item;
	private String complaint;
	private String status;
	private OrderStatus actualStatus;
	private int price;
	private String managerComment;
	private String masterComment;
	private String userComment;
	private int userStars;
}