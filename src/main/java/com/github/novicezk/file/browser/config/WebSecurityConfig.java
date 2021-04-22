package com.github.novicezk.file.browser.config;

import com.github.novicezk.file.browser.FileBrowserProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	@Autowired
	private FileBrowserProperties fileBrowserProperties;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		var registry = http.authorizeRequests();
		registry.antMatchers("/css/**", "/js/**", "/img/**").permitAll();
		if (!this.fileBrowserProperties.isAccessAuthenticated()) {
			registry.antMatchers("/", "/browser**", "/browser/**", "/download**",
					"/view/**", "/tail/**", "/tail-sse**").permitAll();
		}
		registry.anyRequest().authenticated();
		http.formLogin().loginPage("/login").permitAll();
		var logoutConfig = http.logout()
				.logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"));
		if (!this.fileBrowserProperties.isAccessAuthenticated()) {
			logoutConfig.logoutSuccessUrl("/browser");
		}
		logoutConfig.permitAll();
		http.rememberMe().key("file-browser").and().csrf().disable();
	}

	@Bean
	@Override
	public UserDetailsService userDetailsService() {
		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		UserDetails user = User.withUsername(this.fileBrowserProperties.getUsername())
				.password(this.fileBrowserProperties.getPassword())
				.roles("Admin")
				.passwordEncoder(encoder::encode)
				.build();
		return new InMemoryUserDetailsManager(user);
	}

}