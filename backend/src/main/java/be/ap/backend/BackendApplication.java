package be.ap.backend;

import be.ap.backend.repository.CampusRepository;
import be.ap.backend.repository.SchoolRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;

import be.ap.backend.entity.Campus;
import be.ap.backend.entity.School;
import be.ap.backend.entity.User;
import be.ap.backend.entity.UserRole;
import be.ap.backend.repository.UserRepository;

@SpringBootApplication
@RequiredArgsConstructor
public class BackendApplication implements CommandLineRunner {

    private final CampusRepository campusRepository;

    private final SchoolRepository schoolRepository;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.save(new User("admin", passwordEncoder.encode(adminPassword), UserRole.ADMIN));
        }

        if (schoolRepository.count() == 0) {
            School s = schoolRepository.save(new School("AP Hogeschool", "", "", "", "aphogeschool"));
            Campus c = new Campus();
            c.setSchool(s);
            c.setName("Blok A");
            c.setAdres("");
            c.setBorrowLimit(10);
            c.setExtendLimit(3);
            c.setBorrowPeriod(14);
            c.setExtendPeriod(14);
            campusRepository.save(c);
        }
    }
}