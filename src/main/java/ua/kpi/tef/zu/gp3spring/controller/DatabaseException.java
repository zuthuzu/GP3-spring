package ua.kpi.tef.zu.gp3spring.controller;

/**
 * Created by Anton Domin on 2020-03-09
 */
public class DatabaseException extends Exception {
	private boolean duplicate = false;

	public DatabaseException(Exception e) {
		super(e);
	}

	public DatabaseException(String message) {
		super(message);
	}

	public DatabaseException(String message, Exception e) {
		super(message, e);
	}

	public boolean isDuplicate() {
		return duplicate;
	}

	public void setDuplicate(boolean duplicate) {
		this.duplicate = duplicate;
	}
}
