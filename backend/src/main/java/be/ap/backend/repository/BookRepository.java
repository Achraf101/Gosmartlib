package be.ap.backend.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import be.ap.backend.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {

}