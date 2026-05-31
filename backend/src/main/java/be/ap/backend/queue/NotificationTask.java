package be.ap.backend.queue;

import lombok.Data;

@Data
public class NotificationTask implements Runnable {
    public enum Type {
        LOAN,
        REMINDER
    }

    private final Type type;
    private final Long loanId;


    @Override
    public void run() {
        // left empty — execution is handled by TaskProcessor
    }

}
