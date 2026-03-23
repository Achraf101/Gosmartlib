package be.ap.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import be.ap.backend.entity.BookType;

public interface BookTypeRepository extends JpaRepository<BookType, Long> {

    List<BookType> findByNameContainingIgnoreCase(String query);

    BookType findByName(String query);

}
