package ua.kpi.tef.zu.gp3spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.kpi.tef.zu.gp3spring.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by Anton Domin on 2020-03-05
 */
@Repository
public interface UserRepo extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	Optional<User> findByPhone(String phone);

	Optional<User> findByLogin(String login);

	List<User> findByLoginIn(Collection<String> logins);
}
