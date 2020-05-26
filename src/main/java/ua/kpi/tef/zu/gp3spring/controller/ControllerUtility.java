package ua.kpi.tef.zu.gp3spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ua.kpi.tef.zu.gp3spring.dto.OrderDTO;
import ua.kpi.tef.zu.gp3spring.dto.UserDTO;
import ua.kpi.tef.zu.gp3spring.entity.ItemCategory;
import ua.kpi.tef.zu.gp3spring.entity.RoleType;
import ua.kpi.tef.zu.gp3spring.entity.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Anton Domin on 2020-03-23
 */
@Component
public class ControllerUtility {
	private final MessageSource messageSource;
	private MessageSourceAccessor currentMessages;

	@Autowired
	public ControllerUtility(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	public void resetCurrentMessages() {
		currentMessages = new MessageSourceAccessor(messageSource);
	}

	public User getCurrentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		UserDTO currentUser;

		try {
			currentUser = (UserDTO) auth.getPrincipal();
		} catch (ClassCastException e) {
			return new User(); //this is likely wrong, there should be a better way to build dummy objects in Spring
		}

		return currentUser.getUser();
	}

	public List<String> getLocalCategories() {
		return Arrays.stream(ItemCategory.values())
				.map((v) -> getLocalizedText(v.toString()))
				.collect(Collectors.toCollection(ArrayList::new));
	}

	public String getLocalizedText(String property) {
		try {
			return currentMessages.getMessage(property);
		} catch (NoSuchMessageException e) {
			return property;
		}
	}

	public boolean canPlaceNewOrder(User user) {
		return user.getRole() == RoleType.ROLE_USER;
	}

	public boolean canEditThisOrder(User user, OrderDTO order) {
		//TODO detailed state-based tech
		return user.getRole() == order.getLiveState().getRequiredRole();
	}

	public boolean canViewThisOrder(User user, OrderDTO order) {
		return true;
	}
}
