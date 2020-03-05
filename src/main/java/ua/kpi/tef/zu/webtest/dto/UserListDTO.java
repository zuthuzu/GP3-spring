package ua.kpi.tef.zu.webtest.dto;

import lombok.*;
import ua.kpi.tef.zu.webtest.entity.User;

import java.util.List;

/**
 * Created by Anton Domin on 2020-03-05
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class UserListDTO {
	private List<User> users;
}
