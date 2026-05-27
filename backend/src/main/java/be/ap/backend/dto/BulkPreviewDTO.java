package be.ap.backend.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BulkPreviewDTO {

    private final List<PreviewItemDTO> items = new ArrayList<>();

    public void addFound(int row, String isbn, String title, String author, String coverUrl) {
        items.add(new PreviewItemDTO(row, isbn, true, title, author, coverUrl));
    }

    public void addNotFound(int row, String isbn) {
        items.add(new PreviewItemDTO(row, isbn, false, null, null, null));
    }

    public List<PreviewItemDTO> getItems()  { return items; }
    public int getTotal()                   { return items.size(); }
    @JsonProperty("foundCount")
    public int getFoundCount()              { return (int) items.stream().filter(PreviewItemDTO::isFound).count(); }
    @JsonProperty("notFoundCount")
    public int getNotFoundCount()           { return getTotal() - getFoundCount(); }
}