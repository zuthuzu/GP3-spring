package ua.kpi.tef.zu.gp3spring.dto;

import lombok.*;
import ua.kpi.tef.zu.gp3spring.entity.ItemCategory;
import ua.kpi.tef.zu.gp3spring.entity.states.AbstractState;
import ua.kpi.tef.zu.gp3spring.entity.states.OrderStatus;

import java.time.LocalDate;
import java.util.Objects;

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

	public String toStringSkipEmpty() {
		return "OrderDTO{" +
				"id=" + id +
				(Objects.nonNull(creationDate) ? ", creationDate='" + creationDate + '\'' : "") +
				(Objects.nonNull(actualCreationDate) ? ", actualCreationDate='" + actualCreationDate + '\'' : "") +
				(Objects.nonNull(author) ? ", author='" + author + '\'' : "") +
				(Objects.nonNull(authorLogin) ? ", authorLogin='" + authorLogin + '\'' : "") +
				(Objects.nonNull(manager) ? ", manager='" + manager + '\'' : "") +
				(Objects.nonNull(managerLogin) ? ", managerLogin='" + managerLogin + '\'' : "") +
				(Objects.nonNull(master) ? ", master='" + master + '\'' : "") +
				(Objects.nonNull(masterLogin) ? ", masterLogin='" + masterLogin + '\'' : "") +
				(Objects.nonNull(category) ? ", category='" + category + '\'' : "") +
				(Objects.nonNull(actualCategory) ? ", actualCategory='" + actualCategory + '\'' : "") +
				(Objects.nonNull(item) ? ", item='" + item + '\'' : "") +
				(Objects.nonNull(complaint) ? ", complaint='" + complaint + '\'' : "") +
				(Objects.nonNull(status) ? ", status='" + status + '\'' : "") +
				(Objects.nonNull(actualStatus) ? ", actualStatus='" + actualStatus + '\'' : "") +
				(Objects.nonNull(liveState) ? ", liveState='" + liveState + '\'' : "") +
				(price != 0 ? ", price='" + price + '\'' : "") +
				(Objects.nonNull(managerComment) ? ", managerComment='" + managerComment + '\'' : "") +
				(Objects.nonNull(masterComment) ? ", masterComment='" + masterComment + '\'' : "") +
				(Objects.nonNull(userComment) ? ", userComment='" + userComment + '\'' : "") +
				(userStars != 0 ? ", userStars='" + userStars + '\'' : "") +
				(Objects.nonNull(action) ? ", action='" + action + '\'' : "") +
				'}';
	}
}
