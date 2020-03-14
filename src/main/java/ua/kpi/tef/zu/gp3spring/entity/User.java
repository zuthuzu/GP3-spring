package ua.kpi.tef.zu.gp3spring.entity;

import lombok.*;

import javax.persistence.*;

/**
 * Created by Anton Domin on 2020-03-05
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString

@Entity
@Table(name = "user",
		uniqueConstraints = {@UniqueConstraint(columnNames = {"login", "phone", "email"})})
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "id", nullable = false)
	private Long id;
	@Column(nullable = false)
	private String login;
	@Column(nullable = false)
	private String name;
	@Column(nullable = false)
	private String phone;

	//email is optional, but if it's entered, then it should be unique.
	//it's a bit of a grey area, but apparently MySQL unique constraint allows multiple nulls, which is what we need
	@Column
	private String email;

	@Column(name = "role")
	@Enumerated(EnumType.STRING)
	private RoleType role;

	private String password;

	private boolean accountNonExpired;

	private boolean accountNonLocked;

	private boolean credentialsNonExpired;

	private boolean enabled;
}

