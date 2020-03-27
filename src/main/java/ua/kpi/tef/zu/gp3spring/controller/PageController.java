package ua.kpi.tef.zu.gp3spring.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import ua.kpi.tef.zu.gp3spring.dto.LanguageDTO;
import ua.kpi.tef.zu.gp3spring.dto.OrderDTO;
import ua.kpi.tef.zu.gp3spring.entity.RoleType;
import ua.kpi.tef.zu.gp3spring.entity.User;
import ua.kpi.tef.zu.gp3spring.entity.states.AbstractState;
import ua.kpi.tef.zu.gp3spring.service.OrderService;
import ua.kpi.tef.zu.gp3spring.service.UserService;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by Anton Domin on 2020-03-04
 */
@Slf4j
@Controller
public class PageController implements WebMvcConfigurer {

	private LanguageDTO languageSwitcher = new LanguageDTO();
	private final UserService userService;
	private final OrderService orderService;

	@Autowired
	private ControllerUtility utility;

	@Autowired
	public PageController(UserService userService, OrderService orderService) {
		this.userService = userService;
		this.orderService = orderService;
	}

	@Bean
	public LocaleResolver localeResolver() {
		SessionLocaleResolver localeResolver = new SessionLocaleResolver();
		localeResolver.setDefaultLocale(Locale.ENGLISH);
		return localeResolver;
	}

	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
		localeChangeInterceptor.setParamName("l"); //token that is expected after /? in url for locale choice
		return localeChangeInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(localeChangeInterceptor());
	}

	@RequestMapping("/")
	public String mainPage(@RequestParam(value = "error", required = false) String error,
						   @RequestParam(value = "logout", required = false) String logout,
						   @RequestParam(value = "reg", required = false) String reg,
						   Model model) {

		//if we can get user auth from SecurityContextHolder, then he's already logged in, and doesn't need to be here
		if (!SecurityContextHolder.getContext().getAuthentication().getName().equals("anonymousUser")) {
			return lobbyPage(model, null, null, null);
		}

		insertLanguagesIntoModel(model);

		model.addAttribute("error", error != null);
		model.addAttribute("logout", logout != null);
		model.addAttribute("reg", reg != null);

		return "index.html";
	}

	@RequestMapping("/lobby")
	public String lobbyPage(Model model,
							@RequestParam(value = "denied", required = false) String denied,
							@RequestParam(value = "orderfail", required = false) String orderfail,
							@RequestParam(value = "order", required = false) String order) {
		insertLanguagesIntoModel(model);

		model.addAttribute("user", utility.getCurrentUser());
		model.addAttribute("denied", denied != null);
		model.addAttribute("orderfail", orderfail != null);
		model.addAttribute("order", order != null);

		model.addAttribute("activeOrders", getActiveOrders());
		model.addAttribute("secondaryOrders", getSecondaryOrders());

		return "lobby.html";
	}

	@RequestMapping("/users")
	public String usersPage(Model model,
							@RequestParam(value = "error", required = false) String error,
							@RequestParam(value = "success", required = false) String success) {
		insertLanguagesIntoModel(model);

		User currentUser = utility.getCurrentUser();
		model.addAttribute("user", currentUser);

		if (currentUser.getRole() == RoleType.ROLE_ADMIN) {
			model.addAttribute("users", getAllUsers());
			model.addAttribute("error", error != null);
			model.addAttribute("success", success != null);
			return "users.html";
		} else {
			return accessDeniedPage(model);
		}
	}

	@RequestMapping("/reg")
	public String registerUser(@ModelAttribute User user,
							   @RequestParam(value = "error", required = false) String error,
							   @RequestParam(value = "duplicate", required = false) String duplicate,
							   Model model) {

		insertLanguagesIntoModel(model);

		model.addAttribute("nameRegex", "^" + RegistrationValidation.NAME_REGEX + "$");
		model.addAttribute("phoneRegex", "^" + RegistrationValidation.PHONE_REGEX + "$");
		model.addAttribute("loginRegex", "^" + RegistrationValidation.LOGIN_REGEX + "$");

		model.addAttribute("error", error != null);
		model.addAttribute("duplicate", duplicate != null);
		model.addAttribute("newUser", user == null ? new User() : user);

		return "reg.html";
	}

	@RequestMapping("/order")
	public String registerOrder(@ModelAttribute OrderDTO order,
								@RequestParam(value = "error", required = false) String error,
								Model model) {

		insertLanguagesIntoModel(model);
		model.addAttribute("user", utility.getCurrentUser());

		model.addAttribute("categories", utility.getLocalCategories());

		model.addAttribute("error", error != null);
		model.addAttribute("newOrder", order == null ? new OrderDTO() : order);

		return "order-new.html";
	}

	@RequestMapping("/details")
	public String viewOrder(Model model,
							@RequestParam(value = "id", required = false) String id) {

		if (id == null) {
			return accessDeniedPage(model);
		}

		OrderDTO order;

		try {
			order = orderService.getOrderById(id);
		} catch (IllegalArgumentException e) {
			log.error(e.getMessage());
			return accessDeniedPage(model);
		}

		insertLanguagesIntoModel(model);

		User currentUser = utility.getCurrentUser();
		model.addAttribute("user", currentUser);

		setLocalFields(order);
		model.addAttribute("updateOrder", order);

		model.addAttribute("categories", utility.getLocalCategories());

		AbstractState state = order.getLiveState();
		model.addAttribute("submit", utility.getLocalizedText(state.getButtonText()));
		if (currentUser.getRole() == state.getRequiredRole()) {
			model.addAttribute("available", state.getAvailableFields());
			model.addAttribute("cancel", state.isCancelable());
			model.addAttribute("readonly", false);
		} else {
			model.addAttribute("available", Collections.emptyList());
			model.addAttribute("cancel", false);
			model.addAttribute("readonly", true);
		}

		return "order-details.html";
	}

	private String accessDeniedPage(Model model) {
		return lobbyPage(model, "denied", null, null);
	}

	private void insertLanguagesIntoModel(Model model) {
		languageSwitcher.setChoice(LocaleContextHolder.getLocale().toString());
		model.addAttribute("language", languageSwitcher);
		model.addAttribute("supported", languageSwitcher.getSupportedLanguages());
		//this is wrong, there should be a way to recreated the object only on locale switch event
		utility.resetCurrentMessages();
	}

	private List<User> getAllUsers() {
		return userService.getAllUsers().getUsers();
	}

	private List<OrderDTO> getActiveOrders() {
		List<OrderDTO> orders;
		User user = utility.getCurrentUser();
		orders = orderService.getActiveOrders(user);
		orders.forEach(this::setLocalFields);
		return orders;
	}

	private List<OrderDTO> getSecondaryOrders() {
		List<OrderDTO> orders;
		User user = utility.getCurrentUser();
		orders = orderService.getSecondaryOrders(user);
		orders.forEach(this::setLocalFields);
		return orders;
	}

	private void setLocalFields(OrderDTO order) {
		DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(LocaleContextHolder.getLocale());
		order.setCreationDate(order.getActualCreationDate().format(dtf));
		order.setCategory(utility.getLocalizedText(order.getActualCategory().toString()));
		order.setStatus(utility.getLocalizedText(order.getActualStatus().toString()));
	}
}

