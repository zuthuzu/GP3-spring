package ua.kpi.tef.zu.webtest.controller;

import java.util.Arrays;
import java.util.Locale;

/**
 * Created by Anton Domin on 2020-03-04
 */
public class LanguageDTO {
	private String choice;
	private String name;

	public void setChoice(String language) {
		this.choice = language;
		this.name = findNameByCode(language);
	}

	public String getChoice() {
		return choice;
	}

	public String getName() {
		return name;
	}

	public String findNameByCode(String code) {
		String codeLC = code.toLowerCase();

		for (SupportedLanguages lang: SupportedLanguages.values()) {
			if (lang.getCode().equals(codeLC)) {
				return lang.getName();
			}
		}

		return "";
	}

	public String[] getSupportedCodes() {
		return Arrays.stream(SupportedLanguages.values()).map(SupportedLanguages::getCode).toArray(String[]::new);
	}

	public Locale getLocale() {
		return SupportedLanguages.determineLocale(choice);
	}
}
