package ua.kpi.tef.zu.gp3spring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.kpi.tef.zu.gp3spring.controller.DatabaseException;
import ua.kpi.tef.zu.gp3spring.entity.ArchiveOrder;
import ua.kpi.tef.zu.gp3spring.entity.WorkOrder;
import ua.kpi.tef.zu.gp3spring.repository.ArchiveRepo;
import ua.kpi.tef.zu.gp3spring.repository.OrderRepo;

/**
 * Created by Anton Domin on 2020-03-27
 */
@Service
@Transactional(rollbackFor = DatabaseException.class)
public class TransactionService {
	private OrderRepo orderRepo;
	private ArchiveRepo archiveRepo;

	@Autowired
	public TransactionService(OrderRepo orderRepository, ArchiveRepo archiveRepository) {
		this.orderRepo = orderRepository;
		this.archiveRepo = archiveRepository;
	}

	public void archiveOrder(WorkOrder order) throws DatabaseException {
		ArchiveOrder archiveOrder;
		try {
			archiveOrder = (ArchiveOrder) order;
		} catch (ClassCastException e) {
			throw new DatabaseException("Order archivation failed (can't cast to archive class): " + order, e);
		}

		try {
			archiveRepo.save(archiveOrder);
		} catch (Exception e) {
			throw new DatabaseException("Order archivation failed at insert stage: " + order, e);
		}

		try {
			orderRepo.deleteById(order.getId());
		} catch (Exception e) {
			throw new DatabaseException("Order archivation failed at delete stage: " + order, e);
		}
	}
}
