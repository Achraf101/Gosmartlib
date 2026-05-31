package be.ap.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import be.ap.backend.dto.PasswordDTO;
import be.ap.backend.entity.User;
import be.ap.backend.exception.ArgumentsInvalidException;
import be.ap.backend.exception.MissingSessionException;
import be.ap.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void updatePassword(Long userId, PasswordDTO dto) {
        if (userId == null) {
            throw new MissingSessionException("Geen actieve sessie gevonden.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Gebruiker niet gevonden."));

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new ArgumentsInvalidException("Huidig wachtwoord is onjuist.");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

    }
}
