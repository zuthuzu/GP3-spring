package ua.kpi.tef.zu.webtest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import ua.kpi.tef.zu.webtest.dto.LanguageDTO;
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
						   Model model) {
		//our web page can be reloaded via JS without form submitting data into languageSwitcher
		//and locale might change in the meantime. gotta keep it actual for internal purposes
		languageSwitcher.setChoice(LocaleContextHolder.getLocale().toString());

		model.addAttribute("language", languageSwitcher);
		model.addAttribute("supported", languageSwitcher.getSupportedLanguages());

		model.addAttribute("error", error != null);
		model.addAttribute("logout", logout != null);

		return "index.html";
	}

	@RequestMapping("/lobby")
	public String lobbyPage(@ModelAttribute LanguageDTO language, Model model) {
		System.out.println("Obtained locale code from the front end: " + language.getChoice());

		List<User> allUsers = userService.getAllUsers().getUsers();
		if (language.isLocaleCyrillic()) {
			for (User user : allUsers) {
				user.setFirstName(user.getFirstNameCyr());
				user.setLastName(user.getLastNameCyr());
			}
		}

		model.addAttribute("language", language);
		model.addAttribute("users", allUsers);
		return "lobby.html";
	}
}

