package ua.kpi.tef.zu.gp3spring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import ua.kpi.tef.zu.gp3spring.service.UserService;

/**
 * Created by Anton Domin on 2020-03-07
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.authorizeRequests()
					.antMatchers("/", "/reg", "/newuser", "/js/*.js", "/*.css").permitAll()
					.anyRequest().authenticated()
					.and()
				.formLogin()
					.loginPage("/")
					.permitAll()
					.successForwardUrl("/success")
					.and()
				.logout()
					.logoutRequestMatcher(new AntPathRequestMatcher("/logout")).permitAll();
	}

	@Autowired
	private UserService userService;

	@Autowired
	public PasswordEncoder localEncoder;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userService).passwordEncoder(localEncoder);
	}
}
