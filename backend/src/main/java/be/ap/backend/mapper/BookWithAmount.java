package be.ap.backend.mapper;

import be.ap.backend.entity.Book;

public record BookWithAmount(Book book, Integer amount) {
    
}
