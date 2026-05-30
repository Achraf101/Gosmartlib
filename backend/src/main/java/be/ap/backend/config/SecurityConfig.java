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
 * Configureert de beveiliging van de applicatie, waaronder authenticatie,
 * autorisatie en sessiebeheer.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.bcrypt-rounds}")
    private int strength;

    private final UserService userService;

    /**
     * Publiceert HTTP-sessie-events zodat Spring Security wijzigingen in sessies
     * kan opvolgen.
     *
     * @return de publisher voor HTTP-sessie-events
     */
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    /**
     * Configureert de security filter chain met authenticatie, autorisatieregels,
     * login- en logoutafhandeling.
     *
     * @param http de {@link HttpSecurity} configuratie
     * @return de geconfigureerde security filter chain
     * @throws Exception wanneer de securityconfiguratie niet kan worden opgebouwd
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
     * Maakt de password encoder aan die gebruikt wordt voor het hashen en
     * verifiëren van wachtwoorden.
     *
     * @return de geconfigureerde password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(this.strength);
    }

    /**
     * Configureert de authenticatieprovider die gebruikersgegevens ophaalt via
     * de {@link UserService} en wachtwoorden valideert met de geconfigureerde
     * password encoder.
     *
     * @return de geconfigureerde authenticatieprovider
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}