package be.ap.backend.config;

import be.ap.backend.entity.Campus;
import be.ap.backend.entity.School;
import be.ap.backend.entity.User;
import be.ap.backend.repository.CampusRepository;
import be.ap.backend.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.bcrypt-rounds}")
    private int strength;

    private final UserService userService;
    private final CampusRepository campusRepository;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/auth/logout", "/oauth", "/auth/me", "/error").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((req, res, authentication) -> {
                            // custom session attributes
                            Map<String, String> user = getUserDetails(authentication);
                            HttpSession session = req.getSession(true);

                            session.setAttribute("userId", user.get("userId"));
                            session.setAttribute("school", user.get("school"));
                            session.setAttribute("campus", user.get("campus"));
                            session.setAttribute("role", user.get("role"));
                            session.setAttribute("username", user.get("username"));

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
                // uncomment so you can test with bruno using basic auth
                // .httpBasic(Customizer.withDefaults())
                .authenticationProvider(authenticationProvider());
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(this.strength);
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    private Map<String, String> getUserDetails(Authentication auth) {
        User u = (User) auth.getPrincipal();

        School school = u.getSchool();
        List<Campus> campus = campusRepository.findBySchool(school);
        List<Long> campusIds = campus.stream().map(Campus::getId)
                .collect(Collectors.toList());
        System.out.println(campus.toString());

        String schoolString = u.getSchool() != null ? u.getSchool().getId().toString() : "";

        System.out.println(u.getUsername());
        Map<String, String> usr = Map.of(
                "userId", u.getId().toString(),
                "campus", campusIds.toString(),
                "school",
                schoolString,
                "role", u.getRole().name(),
                "username", u.getUsername() // not smartschool name
        );

        return usr;
    }
}
