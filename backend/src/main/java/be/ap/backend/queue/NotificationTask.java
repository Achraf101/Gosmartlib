package be.ap.backend.queue;

import lombok.Data;

/**
 * Task representing a notification to be processed for a loan.
 */
@Data
public class NotificationTask implements Runnable {
    public enum Type {
        LOAN,
        REMINDER
    }

    private final Type type;
    private final Long loanId;

    /**
     * No-op — execution is delegated to {@link TaskProcessor}.
     */
    @Override
    public void run() {
        // left empty — execution is handled by TaskProcessor
    }

}
