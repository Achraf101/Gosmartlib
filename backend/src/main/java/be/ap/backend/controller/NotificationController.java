package be.ap.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.service.NotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("notify")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("{loanId}")
    public ResponseEntity<Boolean> addNotification(@PathVariable Long loanId) {
        // send notification
        if (loanId == null) {
            return ResponseEntity.status(400).body(false);
        }

        return ResponseEntity.ok(notificationService.sendReminderNotification(loanId));
    }
}