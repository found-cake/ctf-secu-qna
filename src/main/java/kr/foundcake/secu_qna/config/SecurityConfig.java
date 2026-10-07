package kr.foundcake.secu_qna.config;

import kr.foundcake.secu_qna.entity.UserAccount;
import kr.foundcake.secu_qna.repository.UserAccountRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(UserAccountRepository users) {
        return username -> {
            UserAccount account = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return User.withUsername(account.getUsername())
                .password(account.getPasswordHash())
                .roles("USER")
                .build();
        };
    }

    @Bean
    HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/register", "/css/**").permitAll()
                .anyRequest().authenticated())
            .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/tickets", false).permitAll())
            .sessionManagement(session -> session.maximumSessions(1).maxSessionsPreventsLogin(true))
            .logout(logout -> logout.logoutSuccessUrl("/login?logout"));
        return http.build();
    }
}
