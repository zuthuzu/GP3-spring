package ua.kpi.tef.zu.webtest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.kpi.tef.zu.webtest.entity.User;

import java.util.Optional;

/**
 * Created by Anton Domin on 2020-03-05
 */
@Repository
public interface UserRepo extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	Optional<User> findByLogin(String login);

	//Optional<User> findByLoginAndPassword(String login, String password);
}
