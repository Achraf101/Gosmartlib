package be.ap.backend.queue;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import be.ap.backend.repository.LoanRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final TaskQueueService taskQueueService;
    private final LoanRepository loanRepository;

    @Scheduled(cron = "0 16 20 * * *", zone = "Europe/Brussels") // every day at 10:00 AM
    public void scheduleDailyNotifications() {
        // fetch all non notified loans for today (loans that need to be returned
        // tomorrow)
        System.out.println("##############################");
        System.out.println("cron job fired");
        List<Long> dueLoans = loanRepository.getDueLoans(LocalDate.now().plusDays(1+13));
        System.out.println("dueLoans.size(): ");
        System.out.println(dueLoans.size());
        // Push them to the queue
        dueLoans.forEach((id) -> {
            taskQueueService.push(new NotificationTask(
                    NotificationTask.Type.REMINDER,
                    id));
        });

        System.out.println("cron jobs added to the queue");

    }
}