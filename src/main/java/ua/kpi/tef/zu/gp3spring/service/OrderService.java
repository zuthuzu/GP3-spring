package ua.kpi.tef.zu.gp3spring.service;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ua.kpi.tef.zu.gp3spring.controller.RegistrationException;
import ua.kpi.tef.zu.gp3spring.dto.OrderDTO;
import ua.kpi.tef.zu.gp3spring.entity.ItemCategory;
import ua.kpi.tef.zu.gp3spring.entity.OrderStatus;
import ua.kpi.tef.zu.gp3spring.entity.WorkOrder;
import ua.kpi.tef.zu.gp3spring.repository.OrderRepo;

/**
 * Created by Anton Domin on 2020-03-22
 */
@Slf4j
@Service
public class OrderService {
	private OrderRepo orderRepo;

	@Autowired
	public OrderService(OrderRepo orderRepository) {
		this.orderRepo = orderRepository;
	}

	public void saveNewOrder(OrderDTO order) throws RegistrationException {
		try {
			orderRepo.save(getPreparedOrder(order));
		} catch (Exception e) {
			log.error("Couldn't save a new order", e);
			throw new RegistrationException(e);
		}
	}

	private WorkOrder getPreparedOrder(OrderDTO order) {
		return WorkOrder.builder()
				.author(order.getAuthor())
				.category(order.getActualCategory())
				.item(order.getItem())
				.complaint(order.getComplaint())
				.status(OrderStatus.PENDING)
				.build();
	}
}
