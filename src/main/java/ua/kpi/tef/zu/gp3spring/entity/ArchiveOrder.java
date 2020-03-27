package ua.kpi.tef.zu.gp3spring.entity;

import lombok.*;

import javax.persistence.*;

/**
 * Created by Anton Domin on 2020-03-14
 */

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "archive")
public class ArchiveOrder extends WorkOrder {
	@Column(name = "user_comment")
	private String userComment;

	@Column(name = "user_stars")
	private int userStars;

	public ArchiveOrder(String userComment, int userStars) {
		this.userComment = userComment;
		this.userStars = userStars;
	}
}