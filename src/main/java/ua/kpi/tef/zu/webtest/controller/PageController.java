package ua.kpi.tef.zu.webtest.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;


/**
 * Created by Anton Domin on 2020-03-04
 */
@Controller
public class PageController {
	/*public PageController() {
		messageSource.setBasename("messages");
		messageSource.setDefaultLocale(SupportedLanguages.getDefaultLocale());
	}*/

	@Bean
	public LocaleResolver localeResolver(){
		SessionLocaleResolver localeResolver = new SessionLocaleResolver();
		localeResolver.setDefaultLocale(Locale.ENGLISH);
		return  localeResolver;
	}

	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor() {
		LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
		localeChangeInterceptor.setParamName("lang");
		return localeChangeInterceptor;
	}

	@RequestMapping("/")
	public String mainPage(Model model){
		model.addAttribute("language", new LanguageDTO());
		model.addAttribute("supported", SupportedLanguages.values());
		return "index.html";
	}

	@PostMapping ("/selected")
	public String processLanguageSelection(@ModelAttribute LanguageDTO language){
		System.out.println(language.getChoice());
		return "selected.html";
	}
}

