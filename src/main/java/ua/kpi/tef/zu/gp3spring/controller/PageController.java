package ua.kpi.tef.zu.gp3spring.controller;

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
import ua.kpi.tef.zu.gp3spring.dto.UserDTO;
import ua.kpi.tef.zu.gp3spring.entity.RoleType;
import ua.kpi.tef.zu.gp3spring.entity.User;
import ua.kpi.tef.zu.gp3spring.service.UserService;

import java.util.List;
import java.util.Locale;

/**
 * Created by Anton Domin on 2020-03-04
 */
@Controller
public class PageController implements WebMvcConfigurer {

	private LanguageDTO languageSwitcher = new LanguageDTO();
	private final UserService userService;

	@Autowired
	public PageController(UserService userService) {
		this.userService = userService;
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
			return lobbyPage(model);
		}

		//our web page can be reloaded via JS without form submitting data into languageSwitcher
		//and locale might change in the meantime. gotta keep it actual for internal purposes
		languageSwitcher.setChoice(LocaleContextHolder.getLocale().toString());

		model.addAttribute("language", languageSwitcher);
		model.addAttribute("supported", languageSwitcher.getSupportedLanguages());

		model.addAttribute("error", error != null);
		model.addAttribute("logout", logout != null);
		model.addAttribute("reg", reg != null);

		return "index.html";
	}

	@RequestMapping("/success")
	public RedirectView localRedirect() {
		RedirectView redirectView = new RedirectView();

		User currentUser = getCurrentUser();
		switch (currentUser.getRole()) {
			case ROLE_ADMIN:
				redirectView.setUrl("/users");
				break;
			case ROLE_MANAGER:
				redirectView.setUrl("/manage");
				break;
			case ROLE_MASTER:
				redirectView.setUrl("/work");
				break;
			case ROLE_USER:
				redirectView.setUrl("/lobby");
				break;
			default:
				redirectView.setUrl("/error");
		}

		return redirectView;
	}

	@RequestMapping("/lobby")
	public String lobbyPage(Model model) {
		model.addAttribute("language", languageSwitcher);
		model.addAttribute("user", getCurrentUser());
		model.addAttribute("error", false);
		return "lobby.html";
	}

	@RequestMapping("/users")
	public String usersPage(Model model) {
		model.addAttribute("language", languageSwitcher);

		User currentUser = getCurrentUser();
		model.addAttribute("user", currentUser);

		//TODO full role split doublecheck
		if (currentUser.getRole() == RoleType.ROLE_ADMIN) {
			model.addAttribute("users", getAllUsers());
			return "users.html";
		} else {
			model.addAttribute("error", true);
			return "lobby.html";
		}
	}

	@RequestMapping("/reg")
	public String registerUser(@ModelAttribute User user,
							   @RequestParam(value = "error", required = false) String error,
							   @RequestParam(value = "duplicate", required = false) String duplicate,
							   Model model) {

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
		System.out.println(modelUser);

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

	private boolean verifyUserFields(User user) {
		return  user.getName().matches(RegistrationValidation.NAME_REGEX) &&
				user.getEmail().matches(RegistrationValidation.EMAIL_REGEX) &&
				user.getPhone().matches(RegistrationValidation.PHONE_REGEX) &&
				user.getLogin().matches(RegistrationValidation.LOGIN_REGEX);
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
}
