package ua.kpi.tef.zu.webtest.entity;

import org.springframework.security.core.GrantedAuthority;

/**
 * Created by Anton Domin on 2020-03-05
 */
public enum RoleType implements GrantedAuthority {
	ROLE_ROOT,
	ROLE_ADMIN,
	ROLE_USER,
	ROLE_USER_WEB,
	ROLE_GUEST;

	@Override
	public String getAuthority() {
		return name();
	}
}