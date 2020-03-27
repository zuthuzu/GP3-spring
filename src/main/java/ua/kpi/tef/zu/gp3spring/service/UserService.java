package ua.kpi.tef.zu.gp3spring.service;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.kpi.tef.zu.gp3spring.controller.DatabaseException;
import ua.kpi.tef.zu.gp3spring.dto.UserDTO;
import ua.kpi.tef.zu.gp3spring.dto.UserListDTO;
import ua.kpi.tef.zu.gp3spring.entity.RoleType;
import ua.kpi.tef.zu.gp3spring.entity.User;
import ua.kpi.tef.zu.gp3spring.repository.UserRepo;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Anton Domin on 2020-03-05
 */

@Slf4j
@Service
public class UserService implements UserDetailsService {
	private UserRepo userRepo;

	@Autowired
	private PasswordEncoder localEncoder;

	@Autowired
	public UserService(UserRepo userRepository) {
		this.userRepo = userRepository;
	}

	@PostConstruct
	private void createSystemAdmin() {
		if (userRepo.findByLogin("admin").isPresent()) {
			return;
		}

		User rawAdminCredentials = User.builder()
				.name("sysadmin")
				.login("admin")
				.phone("0504474405") //maybe replace with some placeholder or default contact?
				.email("admin@zu.tef.kpi.ua")
				.password("admin")
				.role(RoleType.ROLE_ADMIN)
				.build();

		try {
			saveNewUser(rawAdminCredentials);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	public void saveNewUser(User user) throws DatabaseException {
		try {
			userRepo.save(getUserWithPermissions(user));
			log.info("New user created: " + user);
		} catch (DataIntegrityViolationException e) {
			DatabaseException databaseException = new DatabaseException(e);

			if (e.getCause() != null && e.getCause() instanceof ConstraintViolationException) {
				//most likely, either login or email aren't unique
				log.error(((ConstraintViolationException) e.getCause()).getSQLException().getMessage());
				databaseException.setDuplicate(true);
			} else {
				log.error("Couldn't save a new user", e);
			}
			throw databaseException;

		} catch (Exception e) {
			log.error("Couldn't save a new user", e);
			throw new DatabaseException(e);
		}
	}

	public boolean updateRole(String login, String role) {
		if (login.equals("admin")) {
			return false;
		}

		RoleType actualRole;
		try {
			actualRole = RoleType.valueOf(role);
		} catch (IllegalArgumentException e) {
			return false;
		}

		Optional<User> maybeUser = userRepo.findByLogin(login);
		if (!maybeUser.isPresent()) {
			return false;
		}
		User user = maybeUser.get();

		if (user.getRole() == actualRole) {
			return true;
		}

		user.setRole(actualRole);

		try {
			userRepo.save(user);
			log.info("User role updated successfully. User " + user.getLogin() + " is now " + user.getRole());
		} catch (Exception e) {
			log.error("Couldn't update user role", e);
			return false;
		}

		return true;
	}

	private User getUserWithPermissions(User user) {
		return User.builder()
				.name(user.getName())
				.login(user.getLogin())
				.phone(cleanPhoneNumber(user.getPhone()))
				.email(user.getEmail().isEmpty() ? user.getLogin() + "@null" : user.getEmail()) //a makeshift placeholder
				.password(localEncoder.encode(user.getPassword()))
				.role(user.getRole() == null ? RoleType.ROLE_USER : user.getRole())
				.accountNonExpired(true)
				.accountNonLocked(true)
				.credentialsNonExpired(true)
				.enabled(true)
				.build();
	}

	private String cleanPhoneNumber(String rawNumber) {
		StringBuilder result = new StringBuilder();

		for (char n : rawNumber.toCharArray()) {
			if (Character.isDigit(n)) {
				result.append(n);
			}
		}

		return result.toString();
	}

	@Override
	public UserDetails loadUserByUsername(@NotNull String username) throws UsernameNotFoundException {
		return new UserDTO(userRepo.findByLogin(username).orElseThrow(() ->
				new UsernameNotFoundException("login " + username + " not found.")));
	}

	public String getUsernameByLogin(String login) {
		try {
			return (login != null && !login.isEmpty()) ?
					loadUserByUsername(login).toString() : "";
		} catch (UsernameNotFoundException e) {
			log.error(e.getMessage());
			return "";
		}
	}

	public UserListDTO loadUsersByLoginCollection(Set<String> filter) {
		return new UserListDTO(userRepo.findByLoginIn(filter));
	}

	public UserListDTO getAllUsers() {
		return new UserListDTO(userRepo.findAll());
	}
}
