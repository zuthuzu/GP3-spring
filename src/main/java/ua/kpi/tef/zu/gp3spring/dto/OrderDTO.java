package ua.kpi.tef.zu.gp3spring.dto;

import lombok.*;
import ua.kpi.tef.zu.gp3spring.entity.ItemCategory;
import ua.kpi.tef.zu.gp3spring.entity.states.AbstractState;
import ua.kpi.tef.zu.gp3spring.entity.states.OrderStatus;

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
	private String creationDate; //representation in user's locale, refreshed at controller
	private LocalDate actualCreationDate; //value from DB
	private String author;
	private String authorLogin; //value from DB
	private String manager;
	private String managerLogin; //value from DB
	private String master;
	private String masterLogin; //value from DB
	private String category; //representation in user's locale, refreshed at controller
	private ItemCategory actualCategory; //value from DB
	private String item;
	private String complaint;
	private String status; //representation in user's locale, refreshed at controller
	private OrderStatus actualStatus; //value from DB
	private AbstractState liveState;
	private int price;
	private String managerComment;
	private String masterComment;
	private String userComment;
	private int userStars;
	private String action;
}
