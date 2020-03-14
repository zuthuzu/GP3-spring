package ua.kpi.tef.zu.gp3spring.entity;

import org.springframework.security.core.GrantedAuthority;

/**
 * Created by Anton Domin on 2020-03-05
 */
public enum RoleType implements GrantedAuthority {
	ROLE_ADMIN,
	ROLE_MANAGER,
	ROLE_MASTER,
	ROLE_USER;

	@Override
	public String getAuthority() {
		return name();
	}
}