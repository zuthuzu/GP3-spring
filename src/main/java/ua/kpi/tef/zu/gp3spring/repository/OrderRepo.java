package ua.kpi.tef.zu.gp3spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.kpi.tef.zu.gp3spring.entity.WorkOrder;

import java.util.Optional;

/**
 * Created by Anton Domin on 2020-03-22
 */
@Repository
public interface OrderRepo extends JpaRepository<WorkOrder, Long> {
	Optional<WorkOrder> findByAuthor(String author);

	Optional<WorkOrder> findByManager(String manager);

	Optional<WorkOrder> findByMaster(String master);
}
