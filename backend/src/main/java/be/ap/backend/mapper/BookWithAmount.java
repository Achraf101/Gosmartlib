package be.ap.backend.mapper;

import be.ap.backend.entity.Book;

/**
 * Pairs a book with its available stock amount.
 */
public record BookWithAmount(Book book, Integer amount) {

}
