package be.ap.backend.config;

import be.ap.backend.entity.User;
import be.ap.backend.entity.UserRole;
import be.ap.backend.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * Central security configuration for authentication, authorization, and session
 * handling.
 *
 * <p>
 * Defines form login, logout behavior, session management, and role-based
 * access rules.
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.bcrypt-rounds}")
    private int strength;

    private final UserService userService;

    /**
     * Publishes HTTP session lifecycle events to allow Spring Security
     * to track session creation and destruction.
     *
     * @return event publisher for HTTP session monitoring
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    /**
     * Configures the Spring Security filter chain.
     *
     * <p>
     * Includes authentication rules, session policy, login/logout handlers,
     * and exception handling for unauthorized access.
     * </p>
     *
     * @param http Spring Security HTTP configuration
     * @return configured security filter chain
     * @throws Exception if security configuration fails
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/auth/logout", "/oauth", "/auth/current-user",
                                "/error")
                        .permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((req, res, authentication) -> {
                            User u = (User) authentication.getPrincipal();
                            HttpSession session = req.getSession(true);

                            session.setAttribute("userId", u.getId());
                            session.setAttribute("school", u.getSchool() != null ? u.getSchool().getId() : null);
                            session.setAttribute("roles", u.getRoles().stream()
                                    .map(UserRole::name)
                                    .collect(Collectors.toSet()));
                            session.setAttribute("username", u.getUsername());

                            res.setStatus(HttpServletResponse.SC_OK);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\":\"Login successful\"}");
                        })
                        .failureHandler((req, res, exception) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\":\"Invalid credentials\"}");
                        }))
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessHandler((req, res, authentication) -> {
                            res.setStatus(HttpServletResponse.SC_OK);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\":\"Logged out\"}");
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("session"))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, authException) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"message\":\"Unauthorized\"}");
                        }))
                .authenticationProvider(authenticationProvider());
        return http.build();
    }

    /**
     * Provides a BCrypt-based password encoder used for hashing and verifying
     * passwords.
     *
     * @return configured PasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(this.strength);
    }

    /**
     * Configures the authentication provider that delegates user lookup to
     * UserService
     * and validates credentials using the configured password encoder.
     *
     * @return configured DaoAuthenticationProvider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}