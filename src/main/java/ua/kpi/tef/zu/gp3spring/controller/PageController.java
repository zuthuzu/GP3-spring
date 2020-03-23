package ua.kpi.tef.zu.gp3spring.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import ua.kpi.tef.zu.gp3spring.dto.LanguageDTO;
import ua.kpi.tef.zu.gp3spring.dto.OrderDTO;
import ua.kpi.tef.zu.gp3spring.dto.UserDTO;
import ua.kpi.tef.zu.gp3spring.entity.ItemCategory;
import ua.kpi.tef.zu.gp3spring.entity.RoleType;
import ua.kpi.tef.zu.gp3spring.entity.User;
import ua.kpi.tef.zu.gp3spring.service.OrderService;
import ua.kpi.tef.zu.gp3spring.service.UserService;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by Anton Domin on 2020-03-04
 */
@Slf4j
@Controller
public class PageController implements WebMvcConfigurer {

	private LanguageDTO languageSwitcher = new LanguageDTO();
	private ResourceBundle bundle;
	private final static String BUNDLE_NAME = "messages";

	private final UserService userService;
	private final OrderService orderService;

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
			return lobbyPage(model, null, null);
		}

		insertLanguagesIntoModel(model);

		model.addAttribute("error", error != null);
		model.addAttribute("logout", logout != null);
		model.addAttribute("reg", reg != null);

		return "index.html";
	}

	//After log in: determines which home page to call for each user type
	@RequestMapping("/success")
	public RedirectView localRedirect() {
		RedirectView redirectView = new RedirectView();

		User currentUser = getCurrentUser();
		if (currentUser.getRole() == RoleType.ROLE_ADMIN) {
			redirectView.setUrl("/users");
		} else {
			redirectView.setUrl("/lobby");
		}

		return redirectView;
	}

	@RequestMapping("/lobby")
	public String lobbyPage(Model model,
							@RequestParam(value = "error", required = false) String error,
							@RequestParam(value = "order", required = false) String order) {
		insertLanguagesIntoModel(model);

		model.addAttribute("user", getCurrentUser());
		model.addAttribute("error", error != null);
		model.addAttribute("order", order != null);

		model.addAttribute("orders", getOrders());

		return "lobby.html";
	}

	@RequestMapping("/users")
	public String usersPage(Model model,
							@RequestParam(value = "error", required = false) String error,
							@RequestParam(value = "success", required = false) String success) {
		insertLanguagesIntoModel(model);

		User currentUser = getCurrentUser();
		model.addAttribute("user", currentUser);

		if (currentUser.getRole() == RoleType.ROLE_ADMIN) {
			model.addAttribute("users", getAllUsers());
			model.addAttribute("error", error != null);
			model.addAttribute("success", success != null);
			return "users.html";
		} else {
			return lobbyPage(model, "error", null);
		}
	}

	@RequestMapping("/setrole")
	public RedirectView setRole(Model model,
								@RequestParam(value = "login", required = false) String login,
								@RequestParam(value = "role", required = false) String role) {

		RedirectView redirectView = new RedirectView();

		if (login == null || role == null ) {
			redirectView.setUrl("/users?error");
			return redirectView;
		}

		log.info("Admin has initiated a role change: user " + login + " to role " + role);

		boolean howItWent = userService.updateRole(login, role);


		redirectView.setUrl("/users?" + (howItWent ? "success" : "error"));
		return redirectView;
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

	@RequestMapping("/order")
	public String registerOrder(@ModelAttribute OrderDTO order,
							   @RequestParam(value = "error", required = false) String error,
							   Model model) {

		insertLanguagesIntoModel(model);
		model.addAttribute("user", getCurrentUser());

		model.addAttribute("categories", getLocalCategories());

		model.addAttribute("error", error != null);
		model.addAttribute("newOrder", order == null ? new OrderDTO() : order);

		return "order-new.html";
	}

	@RequestMapping("/neworder")
	public RedirectView newOrder(@ModelAttribute OrderDTO modelOrder, RedirectAttributes redirectAttributes) {
		log.info("Obtained new order credentials from front end: " + modelOrder);

		RedirectView redirectView = new RedirectView();

		int categoryIndex = getLocalCategories().indexOf(modelOrder.getCategory());
		if (categoryIndex == -1) {
			redirectAttributes.addFlashAttribute("newOrder", modelOrder);
			redirectView.setUrl("/order?error");
			return redirectView;
		} else {
			modelOrder.setActualCategory(ItemCategory.values()[categoryIndex]);
		}

		modelOrder.setAuthor(getCurrentUser().getLogin());
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

	@RequestMapping("/details")
	public String viewOrder(Model model,
							@RequestParam(value = "id", required = false) String id) {

		if (id == null) {
			return lobbyPage(model, "error", null);
		}

		OrderDTO order;

		try {
			order = orderService.getOrderById(id);
		} catch (IllegalArgumentException e) {
			log.error(e.getMessage());
			return lobbyPage(model, "error", null);
		}

		insertLanguagesIntoModel(model);
		model.addAttribute("user", getCurrentUser());

		setLocalFields(order);
		model.addAttribute("order", order);

		return "order-details.html";
	}

	private List<String> getLocalCategories() {
		List<String> localCategories = new ArrayList<>();
		bundle = ResourceBundle.getBundle(BUNDLE_NAME, LocaleContextHolder.getLocale());

		for (ItemCategory cat : ItemCategory.values()) {
			localCategories.add(getLocalizedText(cat.toString()));
		}

		return localCategories;
	}

	private void insertLanguagesIntoModel(Model model) {
		languageSwitcher.setChoice(LocaleContextHolder.getLocale().toString());
		model.addAttribute("language", languageSwitcher);
		model.addAttribute("supported", languageSwitcher.getSupportedLanguages());
	}

	private boolean verifyUserFields(User user) {
		return  user.getName().matches(RegistrationValidation.NAME_REGEX) &&
				user.getLogin().matches(RegistrationValidation.LOGIN_REGEX) &&
				user.getPhone().matches(RegistrationValidation.PHONE_REGEX) &&
				(user.getEmail().isEmpty() || user.getEmail().matches(RegistrationValidation.EMAIL_REGEX));

	}

	private User getCurrentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		UserDTO currentUser;

		try {
			currentUser = (UserDTO) auth.getPrincipal();
		} catch (ClassCastException e) {
			return new User(); //this is likely wrong, there should be a better way to build dummy objects in Spring
		}

		return currentUser.getUser();
	}

	private List<User> getAllUsers() {
		return userService.getAllUsers().getUsers();
	}

	private List<OrderDTO> getOrders() {
		List<OrderDTO> orders;
		User user = getCurrentUser();

		if (user.getRole() == RoleType.ROLE_MASTER) {
			orders = orderService.getOrdersByMaster(user.getLogin());
		} else if (user.getRole() == RoleType.ROLE_MANAGER) {
			orders = orderService.getOrdersByManager(user.getLogin());
		} else {
			orders = orderService.getOrdersByAuthor(user.getLogin());
		}

		orders.forEach(this::setLocalFields);
		return orders;
	}

	private void setLocalFields(OrderDTO order) {
		bundle = ResourceBundle.getBundle(BUNDLE_NAME, LocaleContextHolder.getLocale());
		DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(LocaleContextHolder.getLocale());
		order.setCreationDate(order.getActualCreationDate().format(dtf));
		order.setCategory(getLocalizedText(order.getActualCategory().toString()));
		order.setStatus(getLocalizedText(order.getActualStatus().toString()));
	}

	public String getLocalizedText(String property) {
		return bundle.keySet().contains(property) ? bundle.getString(property) : property;
	}
}

