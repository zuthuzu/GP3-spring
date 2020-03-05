package ua.kpi.tef.zu.webtest.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.kpi.tef.zu.webtest.dto.UserDTO;
import ua.kpi.tef.zu.webtest.dto.UserListDTO;
import ua.kpi.tef.zu.webtest.entity.User;
import ua.kpi.tef.zu.webtest.repository.UserRepo;

import java.util.Optional;

/**
 * Created by Anton Domin on 2020-03-05
 */

@Service
public class UserService {
	private final UserRepo userRepo;

	@Autowired
	public UserService(UserRepo userRepository) {
		this.userRepo = userRepository;
	}

	public UserListDTO getAllUsers() {
		//TODO checking for an empty user list
		return new UserListDTO(userRepo.findAll());
	}

	public Optional<User> findByUserLogin (UserDTO userDTO){
		//TODO check for user availability. password check
		return userRepo.findByEmail(userDTO.getEmail());
	}

	public void saveNewUser (User user){
		//TODO inform the user about the replay email
		// TODO exception to endpoint
		try {
			userRepo.save(user);
		} catch (Exception ex){
			System.out.println("Error: duplicate user email");
		}

	}
}
