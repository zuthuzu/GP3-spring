package ua.kpi.tef.zu.gp3spring.controller;

/**
 * Created by Anton Domin on 2020-03-09
 */
public interface RegistrationValidation {
	String NAME_REGEX = "[A-Z][a-z]{1,25}";
	String LOGIN_REGEX = "[a-zA-Z][a-zA-Z0-9-_\\.]{1,20}";
	String PHONE_REGEX = "(\\(?[0-9]{3}\\)?)[ .-]?([0-9]{3})[ .-]?([0-9]{2})[ .-]?([0-9]{2})";
	String EMAIL_REGEX = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";
}
