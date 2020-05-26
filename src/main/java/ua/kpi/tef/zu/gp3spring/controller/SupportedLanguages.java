package ua.kpi.tef.zu.gp3spring.controller;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Created by Anton Domin on 2020-02-11
 */

public enum SupportedLanguages {
	ENGLISH ("en", "English"),
	RUSSIAN ("ru", "Русский");

	@SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
	public static final Set<String> CYRILLICS = new HashSet<>(Arrays.asList(RUSSIAN.getCode()));

	private String code;
	private String name;

	SupportedLanguages(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static SupportedLanguages getDefault() {
		return ENGLISH;
	}

	public static Locale getDefaultLocale() {
		return determineLocale(ENGLISH);
	}

	public static Locale determineLocale(String code) {
		String codeLC = code.toLowerCase();
		return determineLocale(Arrays.stream(SupportedLanguages.values())
				.filter(lang -> lang.getCode().equals(codeLC))
				.findAny()
				.orElse(getDefault()));
	}

	public static Locale determineLocale(SupportedLanguages lang) {
		return new Locale(lang.getCode());
	}

}
