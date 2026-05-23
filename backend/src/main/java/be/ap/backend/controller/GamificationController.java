package be.ap.backend.controller;

import be.ap.backend.dto.GamificationDTO;
import be.ap.backend.service.GamificationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;

    @GetMapping
    public ResponseEntity<GamificationDTO> getGamification(HttpSession session) {
        Long userId = Long.valueOf(session.getAttribute("userId").toString());
        return ResponseEntity.ok(gamificationService.getGamification(userId));
    }
}