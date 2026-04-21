package be.ap.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class BulkUploadDTO {

    private int added = 0;
    private final List<RowIssue> skipped = new ArrayList<>();
    private final List<RowIssue> errors = new ArrayList<>();

    public void incrementAdded()                    { this.added++; }
    public void addSkipped(int row, String message) { skipped.add(new RowIssue(row, message)); }
    public void addError(int row, String message)   { errors.add(new RowIssue(row, message)); }

    public int getAdded()                { return added; }
    public List<RowIssue> getSkipped()   { return skipped; }
    public List<RowIssue> getErrors()    { return errors; }
    public int getSkippedCount()         { return skipped.size(); }
    public int getErrorCount()           { return errors.size(); }
    public int getTotalProcessed()       { return added + skipped.size() + errors.size(); }
    public boolean isFullSuccess()       { return skipped.isEmpty() && errors.isEmpty(); }

    public static class RowIssue {
        private final int row;
        private final String message;

        public RowIssue(int row, String message) {
            this.row = row;
            this.message = message;
        }

        public int getRow()        { return row; }
        public String getMessage() { return message; }
    }
}

