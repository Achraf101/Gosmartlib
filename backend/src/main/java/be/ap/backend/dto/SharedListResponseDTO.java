package be.ap.backend.dto;

import java.util.List;

import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookList;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SharedListResponseDTO {
    private BookList list;
    private List<Book> books;
}
