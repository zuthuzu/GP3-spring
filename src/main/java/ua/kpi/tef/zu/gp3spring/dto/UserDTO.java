package ua.kpi.tef.zu.gp3spring.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ua.kpi.tef.zu.gp3spring.entity.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Anton Domin on 2020-03-05
 */

public class UserDTO implements UserDetails {
	private User user;

	public UserDTO(User user) {
		this.user = user;
	}

	public User getUser() {
		return user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Set<GrantedAuthority> result = new HashSet<>();
		result.add(user.getRole());
		return result;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getLogin();
	}

	@Override
	public boolean isAccountNonExpired() {
		return user.isAccountNonExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
		return user.isAccountNonLocked();
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return user.isCredentialsNonExpired();
	}

	@Override
	public boolean isEnabled() {
		return user.isEnabled();
	}
}