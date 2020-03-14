package ua.kpi.tef.zu.gp3spring.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Created by Anton Domin on 2020-03-11
 */

@Component
public class LocalPasswordEncoder implements PasswordEncoder {
	private PasswordEncoder realEncoder;

	public LocalPasswordEncoder() {
		realEncoder = new BCryptPasswordEncoder();
	}

	@Override
	public String encode(CharSequence charSequence) {
		return realEncoder.encode(charSequence);
	}

	@Override
	public boolean matches(CharSequence charSequence, String s) {
		return realEncoder.matches(charSequence, s);
	}
}
