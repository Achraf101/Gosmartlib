package be.ap.backend.dto;

import java.util.ArrayList;
import java.util.List;

public class BulkPreviewDTO {

    private final List<PreviewItem> items = new ArrayList<>();

    public void addFound(int row, String isbn, String title, String author, String coverUrl) {
        items.add(new PreviewItem(row, isbn, true, title, author, coverUrl));
    }

    public void addNotFound(int row, String isbn) {
        items.add(new PreviewItem(row, isbn, false, null, null, null));
    }

    public List<PreviewItem> getItems()  { return items; }
    public int getTotal()                { return items.size(); }
    public int getFoundCount()           { return (int) items.stream().filter(PreviewItem::isFound).count(); }
    public int getNotFoundCount()        { return getTotal() - getFoundCount(); }

    public static class PreviewItem {
        private final int row;
        private final String isbn;
        private final boolean found;
        private final String title;
        private final String author;
        private final String coverUrl;

        public PreviewItem(int row, String isbn, boolean found, String title, String author, String coverUrl) {
            this.row = row;
            this.isbn = isbn;
            this.found = found;
            this.title = title;
            this.author = author;
            this.coverUrl = coverUrl;
        }

        public int getRow()         { return row; }
        public String getIsbn()     { return isbn; }
        public boolean isFound()    { return found; }
        public String getTitle()    { return title; }
        public String getAuthor()   { return author; }
        public String getCoverUrl() { return coverUrl; }
    }
}
