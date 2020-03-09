package ua.kpi.tef.zu.webtest.controller;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import ua.kpi.tef.zu.webtest.dto.LanguageDTO;
import ua.kpi.tef.zu.webtest.dto.UserDTO;
import ua.kpi.tef.zu.webtest.entity.RoleType;
import ua.kpi.tef.zu.webtest.entity.User;
import ua.kpi.tef.zu.webtest.service.UserService;

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
	private PasswordEncoder passwordEncoder;

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

		if (currentUserIsAdmin()) {
			redirectView.setUrl("/users");
		} else {
			redirectView.setUrl("/lobby");
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
		model.addAttribute("user", getCurrentUser());
		if (currentUserIsAdmin()) {
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

		model.addAttribute("firstNameRegex", "^" + RegistrationValidation.FIRST_NAME_REGEX + "$");
		model.addAttribute("firstNameCyrRegex", "^" + RegistrationValidation.FIRST_NAME_CYR_REGEX + "$");
		model.addAttribute("lastNameRegex", "^" + RegistrationValidation.LAST_NAME_REGEX + "$");
		model.addAttribute("lastNameCyrRegex", "^" + RegistrationValidation.LAST_NAME_CYR_REGEX + "$");
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
			userService.saveNewUser(getUserWithPermissions(modelUser));

		} catch (DataIntegrityViolationException e) {

			if (e.getCause() != null && e.getCause() instanceof ConstraintViolationException) {
				//most likely, either login or email aren't unique
				System.out.println(((ConstraintViolationException) e.getCause()).getSQLException().getMessage());
				redirectView.setUrl("/reg?duplicate");
			} else {
				e.printStackTrace();
				redirectView.setUrl("/reg?error");
			}

		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("user", modelUser);
			redirectView.setUrl("/reg?error");
		}

		redirectView.setUrl("/?reg");
		return redirectView;
	}

	private User getUserWithPermissions(User user) {
		return User.builder()
					.firstName(user.getFirstName())
					.firstNameCyr(user.getFirstNameCyr())
					.lastName(user.getLastName())
					.lastNameCyr(user.getLastNameCyr())
					.login(user.getLogin())
					.email(user.getEmail())
					.password(passwordEncoder.encode(user.getPassword()))
					.role(RoleType.ROLE_USER)
					.accountNonExpired(true)
					.accountNonLocked(true)
					.credentialsNonExpired(true)
					.enabled(true)
					.build();
	}

	private boolean verifyUserFields(User user) {
		return  user.getFirstName().matches(RegistrationValidation.FIRST_NAME_REGEX) &&
				user.getFirstNameCyr().matches(RegistrationValidation.FIRST_NAME_CYR_REGEX) &&
				user.getLastName().matches(RegistrationValidation.LAST_NAME_REGEX) &&
				user.getLastNameCyr().matches(RegistrationValidation.LAST_NAME_CYR_REGEX) &&
				user.getLogin().matches(RegistrationValidation.LOGIN_REGEX);
	}

	private boolean currentUserIsAdmin() {
		User currentUser = getCurrentUser();
		return currentUser.getRole() == RoleType.ROLE_ADMIN || currentUser.getRole() == RoleType.ROLE_ROOT;
	}

	private User getCurrentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		UserDTO currentUser;

		try {
			currentUser = (UserDTO) auth.getPrincipal();
		} catch (ClassCastException e) {
			return new User(); //this is likely wrong, there should be a better way to build dummy objects in Spring
		}

		substituteWithCyrillic(currentUser.getUser());
		return currentUser.getUser();
	}

	private List<User> getAllUsers() {
		List<User> users = userService.getAllUsers().getUsers();
		users.forEach(this::substituteWithCyrillic);
		return users;
	}

	private void substituteWithCyrillic (User user) {
		if (languageSwitcher.isLocaleCyrillic()) {
			user.setFirstName(user.getFirstNameCyr());
			user.setLastName(user.getLastNameCyr());
		}
	}
}

