package be.ap.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.queue.NotificationTask;
import be.ap.backend.queue.TaskQueueService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("notify")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BIBLIOTHEEKBEHEERDER')")
public class NotificationController {

    private final TaskQueueService taskQueueService;


    @PostMapping("{loanId}")
    public ResponseEntity<Boolean> addNotification(@PathVariable Long loanId) {
        // send notification
        if (loanId == null) {
            return ResponseEntity.status(400).body(false);
        }

        taskQueueService.push(new NotificationTask(NotificationTask.Type.REMINDER, loanId));

        return ResponseEntity.ok(true);
    }
}