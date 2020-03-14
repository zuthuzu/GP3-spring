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
import ua.kpi.tef.zu.gp3spring.controller.RegistrationException;
import ua.kpi.tef.zu.gp3spring.dto.UserDTO;
import ua.kpi.tef.zu.gp3spring.dto.UserListDTO;
import ua.kpi.tef.zu.gp3spring.entity.RoleType;
import ua.kpi.tef.zu.gp3spring.entity.User;
import ua.kpi.tef.zu.gp3spring.repository.UserRepo;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

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

	public UserListDTO getAllUsers() {
		return new UserListDTO(userRepo.findAll());
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
		} catch (RegistrationException e) {
			e.printStackTrace();
		}
	}

	public void saveNewUser(User user) throws RegistrationException {
		try {
			userRepo.save(getUserWithPermissions(user));
		} catch (DataIntegrityViolationException e) {
			RegistrationException registrationException = new RegistrationException(e);

			if (e.getCause() != null && e.getCause() instanceof ConstraintViolationException) {
				//most likely, either login or email aren't unique
				log.info(((ConstraintViolationException) e.getCause()).getSQLException().getMessage());
				registrationException.setDuplicate(true);
			} else {
				e.printStackTrace();
			}
			throw registrationException;

		} catch (Exception e) {
			e.printStackTrace();
			throw new RegistrationException(e);
		}
	}

	private User getUserWithPermissions(User user) {
		return User.builder()
				.name(user.getName())
				.login(user.getLogin())
				.phone(cleanPhoneNumber(user.getPhone()))
				.email(user.getEmail())
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
}
