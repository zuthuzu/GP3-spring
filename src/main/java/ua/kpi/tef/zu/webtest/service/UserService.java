package ua.kpi.tef.zu.webtest.service;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ua.kpi.tef.zu.webtest.controller.RegistrationException;
import ua.kpi.tef.zu.webtest.dto.UserDTO;
import ua.kpi.tef.zu.webtest.dto.UserListDTO;
import ua.kpi.tef.zu.webtest.entity.User;
import ua.kpi.tef.zu.webtest.repository.UserRepo;

import javax.validation.constraints.NotNull;

/**
 * Created by Anton Domin on 2020-03-05
 */

@Service
public class UserService implements UserDetailsService {
	private UserRepo userRepo;

	@Autowired
	public UserService(UserRepo userRepository) {
		this.userRepo = userRepository;
	}

	public UserListDTO getAllUsers() {
		return new UserListDTO(userRepo.findAll());
	}

	public void saveNewUser(User user) throws RegistrationException {
		try {
			userRepo.save(user);
		} catch (DataIntegrityViolationException e) {
			RegistrationException registrationException = new RegistrationException(e);

			if (e.getCause() != null && e.getCause() instanceof ConstraintViolationException) {
				//most likely, either login or email aren't unique
				System.out.println(((ConstraintViolationException) e.getCause()).getSQLException().getMessage());
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

	@Override
	public UserDetails loadUserByUsername(@NotNull String username) throws UsernameNotFoundException {
		return new UserDTO(userRepo.findByLogin(username).orElseThrow(() ->
				new UsernameNotFoundException("login " + username + " not found.")));
	}
}
