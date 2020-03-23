package ua.kpi.tef.zu.gp3spring.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.kpi.tef.zu.gp3spring.controller.RegistrationException;
import ua.kpi.tef.zu.gp3spring.dto.OrderDTO;
import ua.kpi.tef.zu.gp3spring.entity.OrderStatus;
import ua.kpi.tef.zu.gp3spring.entity.WorkOrder;
import ua.kpi.tef.zu.gp3spring.repository.OrderRepo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
				.creationDate(LocalDate.now())
				.build();
	}

	public OrderDTO getOrderById(String id) throws IllegalArgumentException {
		long realID;
		try {
			realID = Long.parseLong(id);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Can't convert ID " + id + " to a number.");
		}

		WorkOrder order = orderRepo.findById(realID).orElseThrow(() -> new IllegalArgumentException("Can't find order with ID " + id));
		return wrapOrderInDTO(order);
	}

	public List<OrderDTO> getOrdersByMaster(String login) {
		return wrapWorkCollectionInDTO(orderRepo.findByMaster(login));
	}

	public List<OrderDTO> getOrdersByManager(String login) {
		return wrapWorkCollectionInDTO(orderRepo.findByManager(login));
	}

	public List<OrderDTO> getOrdersByAuthor(String login) {
		return wrapWorkCollectionInDTO(orderRepo.findByAuthor(login));
	}

	private List<OrderDTO> wrapWorkCollectionInDTO(List<WorkOrder> entities) {
		List<OrderDTO> result = new ArrayList<>();
		for (WorkOrder order : entities) {
			result.add(wrapOrderInDTO(order));
		}
		return result;
	}

	private OrderDTO wrapOrderInDTO(WorkOrder order) {
		return OrderDTO.builder()
				.id(order.getId())
				.actualCreationDate(order.getCreationDate())
				.author(order.getAuthor())
				.manager(order.getManager())
				.master(order.getMaster())
				.actualCategory(order.getCategory())
				.item(order.getItem())
				.complaint(order.getComplaint())
				.actualStatus(order.getStatus())
				.price(order.getPrice())
				.managerComment(order.getManagerComment())
				.masterComment(order.getMasterComment())
				.build();
	}
}
