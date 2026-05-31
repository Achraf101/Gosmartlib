package be.ap.backend.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.Value;

/**
 * Data transfer object representing the result of a bulk book upload operation.
 *
 * <p>
 * Tracks successfully added books, skipped rows, validation errors,
 * and incomplete records encountered during processing.
 * Also provides derived metrics for reporting and UI feedback.
 * </p>
 */
@Data
public class BulkUploadDTO {

    private int added = 0;
    private final List<RowIssue> skipped = new ArrayList<>();
    private final List<RowIssue> errors = new ArrayList<>();
    private final List<IncompleteBookDTO> incomplete = new ArrayList<>();
    private final List<AddedBook> addedBooks = new ArrayList<>();

    public void incrementAdded() {
        this.added++;
    }

    public void addSkipped(int row, String message) {
        skipped.add(new RowIssue(row, message));
    }

    public void addError(int row, String message) {
        errors.add(new RowIssue(row, message));
    }

    public void addIncomplete(IncompleteBookDTO book) {
        incomplete.add(book);
    }

    public void addAddedBook(Long id, String title) {
        addedBooks.add(new AddedBook(id, title));
    }

    public int getSkippedCount() {
        return skipped.size();
    }

    public int getErrorCount() {
        return errors.size();
    }

    public int getIncompleteCount() {
        return incomplete.size();
    }

    public int getTotalProcessed() {
        return added + skipped.size() + errors.size();
    }

    public boolean isFullSuccess() {
        return skipped.isEmpty() && errors.isEmpty();
    }

    @Value
    public static class RowIssue {
        private final int row;
        private final String message;
    }

    @Value
    public static class AddedBook {
        private final Long id;
        private final String title;
    }
}
