package ua.kpi.tef.zu.gp3spring.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import ua.kpi.tef.zu.gp3spring.dto.OrderDTO;
import ua.kpi.tef.zu.gp3spring.entity.ItemCategory;
import ua.kpi.tef.zu.gp3spring.entity.RoleType;
import ua.kpi.tef.zu.gp3spring.entity.User;
import ua.kpi.tef.zu.gp3spring.service.OrderService;
import ua.kpi.tef.zu.gp3spring.service.UserService;

/**
 * Created by Anton Domin on 2020-03-23
 */
@Slf4j
@Controller
public class RedirectController {
	private final UserService userService;
	private final OrderService orderService;

	@Autowired
	private ControllerUtility utility;

	@Autowired
	public RedirectController(UserService userService, OrderService orderService) {
		this.userService = userService;
		this.orderService = orderService;
	}


	//After log in: determines which home page to call for each user type
	@RequestMapping("/success")
	public RedirectView localRedirect() {
		RedirectView redirectView = new RedirectView();

		User currentUser = utility.getCurrentUser();
		if (currentUser.getRole() == RoleType.ROLE_ADMIN) {
			redirectView.setUrl("/users");
		} else {
			redirectView.setUrl("/lobby");
		}

		return redirectView;
	}

	//Admin only tool: changes user roles
	@RequestMapping("/setrole")
	public RedirectView setRole(Model model,
								@RequestParam(value = "login", required = false) String login,
								@RequestParam(value = "role", required = false) String role) {

		RedirectView redirectView = new RedirectView();

		if (login == null || role == null) {
			redirectView.setUrl("/users?error");
			return redirectView;
		}

		log.info(utility.getCurrentUser().getLogin() + " has initiated a role change: user " + login + " to role " + role);

		boolean howItWent = userService.updateRole(login, role);

		redirectView.setUrl("/users?" + (howItWent ? "success" : "error"));
		return redirectView;
	}

	@RequestMapping("/newuser")
	public RedirectView newUser(@ModelAttribute User modelUser, RedirectAttributes redirectAttributes) {
		log.info("Obtained new user credentials from front end: " + modelUser);

		RedirectView redirectView = new RedirectView();

		if (!verifyUserFields(modelUser)) {
			redirectAttributes.addFlashAttribute("user", modelUser);
			redirectView.setUrl("/reg?error");
			return redirectView;
		}

		try {
			userService.saveNewUser(modelUser);
		} catch (RegistrationException e) {
			redirectAttributes.addFlashAttribute("user", modelUser);
			redirectView.setUrl(e.isDuplicate() ? "/reg?duplicate" : "/reg?error");
			return redirectView;
		}

		redirectView.setUrl("/?reg");
		return redirectView;
	}

	@RequestMapping("/neworder")
	public RedirectView newOrder(@ModelAttribute OrderDTO modelOrder, RedirectAttributes redirectAttributes) {
		log.info("Obtained new order credentials from front end: " + modelOrder.toStringSkipEmpty());

		RedirectView redirectView = new RedirectView();

		if (!restoreCategoryFromLocalView(modelOrder)) {
			redirectAttributes.addFlashAttribute("newOrder", modelOrder);
			redirectView.setUrl("/order?error");
			return redirectView;
		}

		modelOrder.setAuthor(utility.getCurrentUser().getLogin());
		try {
			orderService.saveNewOrder(modelOrder);
		} catch (RegistrationException e) {
			redirectAttributes.addFlashAttribute("newOrder", modelOrder);
			redirectView.setUrl("/order?error");
			return redirectView;
		}

		redirectView.setUrl("/lobby?order");
		return redirectView;
	}

	@RequestMapping("/updateorder")
	public RedirectView updateOrder(@ModelAttribute OrderDTO modelOrder, RedirectAttributes redirectAttributes) {
		log.info("Order update request from front end: " + modelOrder.toStringSkipEmpty());
		RedirectView redirectView = new RedirectView();
		restoreCategoryFromLocalView(modelOrder);
		redirectView.setUrl(orderService.updateOrder(modelOrder, utility.getCurrentUser()) ? "/lobby?order" : "lobby?orderfail");
		return redirectView;
	}

	private boolean verifyUserFields(User user) {
		return user.getName().matches(RegistrationValidation.NAME_REGEX) &&
				user.getLogin().matches(RegistrationValidation.LOGIN_REGEX) &&
				user.getPhone().matches(RegistrationValidation.PHONE_REGEX) &&
				(user.getEmail().isEmpty() || user.getEmail().matches(RegistrationValidation.EMAIL_REGEX));

	}

	private boolean restoreCategoryFromLocalView(OrderDTO modelOrder) {
		int categoryIndex = utility.getLocalCategories().indexOf(modelOrder.getCategory());
		if (categoryIndex == -1) {
			return false;
		} else {
			modelOrder.setActualCategory(ItemCategory.values()[categoryIndex]);
			return true;
		}
	}
}
