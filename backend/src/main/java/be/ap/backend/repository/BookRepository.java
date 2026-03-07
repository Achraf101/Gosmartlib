package be.ap.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import be.ap.backend.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findBy();

    Page<Book> findAll(Pageable pageable);
}
