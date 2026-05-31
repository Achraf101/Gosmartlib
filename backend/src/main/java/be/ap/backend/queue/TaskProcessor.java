package be.ap.backend.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import be.ap.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;

/**
 * Asynchronous processor that drains the notification task queue.
 * At most one worker runs at a time; concurrent invocations return immediately.
 */
@Service
@RequiredArgsConstructor
public class TaskProcessor {

    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private final NotificationService notificationService;

    /**
     * Drains and processes all pending tasks in the given queue.
     * Concurrent calls are silently ignored while a worker is already running.
     */
    @Async("taskExecutor")
    public void processTasks(BlockingQueue<NotificationTask> taskQueue) {
        // If already processing, a worker is running — just return
        if (!isProcessing.compareAndSet(false, true)) {
            return;
        }

        try {
            NotificationTask task;
            while ((task = taskQueue.poll()) != null) {
                notificationService.sendNotification(task.getLoanId(), task.getType());
            }
        } finally {
            isProcessing.set(false);
        }
    }
}