package be.ap.backend.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.Value;

@Data
public class BulkUploadDTO {

    private int added = 0;
    private final List<RowIssue> skipped = new ArrayList<>();
    private final List<RowIssue> errors = new ArrayList<>();
    private final List<IncompleteBookDTO> incomplete = new ArrayList<>();

    public void incrementAdded()                    { this.added++; }
    public void addSkipped(int row, String message) { skipped.add(new RowIssue(row, message)); }
    public void addError(int row, String message)   { errors.add(new RowIssue(row, message)); }
    public void addIncomplete(IncompleteBookDTO book)   { incomplete.add(book); }

    public int getSkippedCount()         { return skipped.size(); }
    public int getErrorCount()           { return errors.size(); }
    public int getIncompleteCount()      { return incomplete.size(); }
    public int getTotalProcessed()       { return added + skipped.size() + errors.size(); }
    public boolean isFullSuccess()       { return skipped.isEmpty() && errors.isEmpty(); }

    @Value
    public static class RowIssue {
        private final int row;
        private final String message;
    }
}

