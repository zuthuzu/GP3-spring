package ua.kpi.tef.zu.gp3spring.dto;

import lombok.*;
import ua.kpi.tef.zu.gp3spring.entity.ItemCategory;

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
	private String author;
	private String category;
	private ItemCategory actualCategory;
	private String item;
	private String complaint;
}
