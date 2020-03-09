package ua.kpi.tef.zu.webtest.controller;

import org.springframework.dao.DataIntegrityViolationException;

/**
 * Created by Anton Domin on 2020-03-09
 */
public class RegistrationException extends Exception {
	private boolean duplicate = false;

	public RegistrationException(Exception e) {
		super(e);
	}

	public boolean isDuplicate() {
		return duplicate;
	}

	public void setDuplicate(boolean duplicate) {
		this.duplicate = duplicate;
	}
}
