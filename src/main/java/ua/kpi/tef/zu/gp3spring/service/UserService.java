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
import ua.kpi.tef.zu.gp3spring.entity.RoleType;
import ua.kpi.tef.zu.gp3spring.entity.User;
import ua.kpi.tef.zu.gp3spring.repository.UserRepo;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * Created by Anton Domin on 2020-03-05
 */

@Slf4j
@Service
public class UserService implements UserDetailsService {
	private final static String SYSADMIN = "admin";
	private final static String DEFAULT_EMAIL_DOMAIN = "@null";
	private final UserRepo userRepo;
	private final PasswordEncoder localEncoder;

	@Autowired
	public UserService(UserRepo userRepository, PasswordEncoder localEncoder) {
		this.userRepo = userRepository;
		this.localEncoder = localEncoder;
	}

	@PostConstruct
	private void createSystemAdmin() {
		if (userRepo.findByLogin(SYSADMIN).isPresent()) {
			return;
		}

		User rawAdminCredentials = User.builder()
				.name("sysadmin")
				.login(SYSADMIN)
				.phone("0000000000")
				.email(SYSADMIN + DEFAULT_EMAIL_DOMAIN)
				.password(SYSADMIN) //
				.role(RoleType.ROLE_ADMIN)
				.build();

		try {
			saveNewUser(rawAdminCredentials);
		} catch (DatabaseException e) {
			log.error(e.getMessage());
		}
	}

	public void updateRole(String login, String role) throws DatabaseException, IllegalArgumentException {
		if (login.equals(SYSADMIN)) throw new IllegalArgumentException("Attempt to modify a protected user");

		RoleType actualRole = RoleType.valueOf(role);
		User user = userRepo.findByLogin(login).orElseThrow(() ->
				new IllegalArgumentException("Login " + login + " not found."));

		if (user.getRole() == actualRole) return;

		user.setRole(actualRole);
		saveExistingUser(user);
	}

	public void saveExistingUser(User user) throws DatabaseException {
		saveUser(user);
		log.info("User " + user.getLogin() + " updated successfully. Role is now " + user.getRole());
	}

	public void saveNewUser(User user) throws DatabaseException {
		saveUser(getUserWithPermissions(user));
		log.info("New user created: " + user);
	}

	private void saveUser(User user) throws DatabaseException {
		try {
			userRepo.save(user);
		} catch (DataIntegrityViolationException e) {
			DatabaseException dbe = new DatabaseException("Couldn't save a user: " + user, e);
			if (e.getCause() != null && e.getCause() instanceof ConstraintViolationException) {
				//most likely, either login or email aren't unique
				//TODO: proper check which one of those is it
				log.error(((ConstraintViolationException) e.getCause()).getSQLException().getMessage());
				dbe.setDuplicate(true);
			}
			throw dbe;

		} catch (Exception e) {
			throw new DatabaseException("Couldn't save a user: " + user, e);
		}
	}

	private User getUserWithPermissions(User user) {
		return User.builder()
				.name(user.getName())
				.login(user.getLogin())
				.phone(cleanPhoneNumber(user.getPhone()))
				.email(user.getEmail().isEmpty() ? user.getLogin() + DEFAULT_EMAIL_DOMAIN : user.getEmail())
				.password(localEncoder.encode(user.getPassword()))
				.role(user.getRole() == null ? RoleType.ROLE_USER : user.getRole())
				.accountNonExpired(true)
				.accountNonLocked(true)
				.credentialsNonExpired(true)
				.enabled(true)
				.build();
	}

	private String cleanPhoneNumber(String rawNumber) {
		return rawNumber.chars()
				.filter(Character::isDigit)
				.collect(StringBuilder::new,
						StringBuilder::appendCodePoint,
						StringBuilder::append)
				.toString();
	}

	@Override
	public UserDetails loadUserByUsername(@NotNull String login) throws UsernameNotFoundException {
		return new UserDTO(userRepo.findByLogin(login).orElseThrow(() ->
				new UsernameNotFoundException("Login " + login + " not found.")));
	}

	public List<User> loadUsersByLoginCollection(Set<String> filter) {
		return userRepo.findByLoginIn(filter);
	}

	public List<User> getAllUsers() {
		return userRepo.findAll();
	}
}
