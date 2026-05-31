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

/**
 * REST controller responsible for queuing notification-related tasks.
 * <p>
 * Only users with the role {@code BIBLIOTHEEKBEHEERDER} are authorized to
 * access these endpoints.
 * </p>
 */
@RestController
@RequestMapping("notify")
@RequiredArgsConstructor
@PreAuthorize("hasRole('BIBLIOTHEEKBEHEERDER')")
public class NotificationController {

    private final TaskQueueService taskQueueService;

    /**
     * Queues a reminder notification for a specific loan.
     *
     * @param loanId the identifier of the loan for which a reminder notification
     *               should be sent
     * @return {@code true} if the notification task was successfully queued
     */
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