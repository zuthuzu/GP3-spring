package ua.kpi.tef.zu.webtest.controller;

/**
 * Created by Anton Domin on 2020-03-09
 */
public interface RegistrationValidation {
	String FIRST_NAME_REGEX = "[A-Z][a-z]{1,25}";
	String FIRST_NAME_CYR_REGEX = "[A-ЯЁҐІЇЄ][а-яёґіїє']{1,25}";
	String LAST_NAME_REGEX = "[a-zA-Z]{1,}'?-?[a-zA-Z]{2,}\\s?([a-zA-Z]{1,})?";
	String LAST_NAME_CYR_REGEX = "[А-ЯЁҐІЇЄ][а-яёґіїє']{1,25}([-][А-ЯЁҐІЇЄ][а-яёґіїє']{1,25})?";
	String LOGIN_REGEX = "[a-zA-Z][a-zA-Z0-9-_\\.]{1,20}";
}
