package ua.kpi.tef.zu.gp3spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.kpi.tef.zu.gp3spring.entity.ArchiveOrder;

import java.util.List;
import java.util.Optional;

/**
 * Created by Anton Domin on 2020-03-22
 */
@Repository
public interface ArchiveRepo extends JpaRepository<ArchiveOrder, Long> {
	Optional<ArchiveOrder> findById(long id);

	List<ArchiveOrder> findByAuthor(String author);
}
