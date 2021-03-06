package ua.kpi.tef.zu.gp3spring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.kpi.tef.zu.gp3spring.entity.states.OrderStatus;
import ua.kpi.tef.zu.gp3spring.entity.WorkOrder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by Anton Domin on 2020-03-22
 */
@Repository
public interface OrderRepo extends JpaRepository<WorkOrder, Long> {
	Optional<WorkOrder> findById(long id);

	List<WorkOrder> findByStatusIn(Collection<OrderStatus> statuses);

	List<WorkOrder> findByAuthor(String author);

	List<WorkOrder> findByAuthorAndStatusIn(String author, Collection<OrderStatus> statuses);

	List<WorkOrder> findByManager(String manager);

	List<WorkOrder> findByManagerAndStatusIn(String manager, Collection<OrderStatus> statuses);

	List<WorkOrder> findByMaster(String master);

	List<WorkOrder> findByMasterAndStatusIn(String master, Collection<OrderStatus> statuses);
}
