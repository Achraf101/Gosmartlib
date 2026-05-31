package be.ap.backend.controller;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.GamificationDTO;
import be.ap.backend.exception.MissingSessionException;
import be.ap.backend.service.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;
    private final SessionContext sessionContext;

    @GetMapping
    public ResponseEntity<GamificationDTO> getGamification() {
        Object raw = sessionContext.getUserId();
        if (raw == null)
            throw new MissingSessionException("Niet ingelogd");
        Long userId = Long.valueOf(raw.toString());
        return ResponseEntity.ok(gamificationService.getGamification(userId));
    }
}