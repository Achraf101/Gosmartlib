package be.ap.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import be.ap.backend.dto.BookCardDTO;
import be.ap.backend.entity.Book;
import jakarta.transaction.Transactional;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findBy();

    Page<Book> findAll(Pageable pageable);

    @Query("SELECT new be.ap.backend.dto.BookCardDTO(b.id, b.title, b.cover, a.name) FROM Book b JOIN b.author a WHERE b.author = (SELECT b.author FROM Book b WHERE b.id=:id) AND b.id != :id")
    List<BookCardDTO> findRelated(@Param("id") Long id);

    @Query("SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN b.author a " +
            "LEFT JOIN b.genres g " +
            "WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Book> search(@Param("query") String query, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Book b SET b.cover = :filename WHERE b.id = :id")
    int updateCover(@Param("id") Long id, @Param("filename") String filename);
}
