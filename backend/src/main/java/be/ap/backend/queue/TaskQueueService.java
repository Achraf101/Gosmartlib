package be.ap.backend.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskQueueService {

    private final BlockingQueue<NotificationTask> taskQueue = new LinkedBlockingQueue<>();
    private final TaskProcessor taskProcessor;


    public void push(NotificationTask task) {
        taskQueue.offer(task);
        taskProcessor.processTasks(taskQueue);
    }
}