package be.ap.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import be.ap.backend.dto.BookCardDTO;
import be.ap.backend.dto.GenreProjection;
import be.ap.backend.dto.BookResultDTO;
import be.ap.backend.entity.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findBy();

    Page<Book> findAll(Pageable pageable);

    @Query("""
    SELECT DISTINCT new be.ap.backend.dto.BookCardDTO(
        b.id,
        b.title,
        b.cover,
        b.author.name
        )
    FROM Book b
    JOIN b.genres g
    WHERE g IN (
        SELECT g2
        FROM Book b2
        JOIN b2.genres g2
        WHERE b2.id = :id
    )
    AND b.id != :id
    """)
    List<BookCardDTO> findRelated(@Param("id") Long id);

    @Query("SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN b.author a " +
            "LEFT JOIN b.genres g " +
            "WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%'))" +
            "OR b.isbn LIKE CONCAT('%', :query, '%')")
    Page<Book> search(@Param("query") String query, Pageable pageable);

    @Query(value = """
    SELECT new be.ap.backend.dto.BookResultDTO(
        b.id,
        b.title,
        b.cover,
        b.author.name,
        b.bookType,
        b.seriesId,
        b.seriesCount,
        b.language,
        b.published,
        b.description,
        b.fiction,
        b.ageStart,
        b.ageEnd,
        b.pages,
        b.rating,
        b.ratingCount
    )
    FROM Book b
    ORDER BY b.id ASC
    """)
    Page<BookResultDTO> getAllBookResults(Pageable pageable);

    @Query("""
    SELECT b.id, g.id, g.name
    FROM Book b
    JOIN b.genres g
    WHERE b.id IN :bookIds
    """)
    List<GenreProjection> findGenresForBooks(@Param("bookIds") List<Long> bookIds);
    @Query("SELECT DISTINCT b FROM Book b " +
            "LEFT JOIN b.author a " +
            "LEFT JOIN b.genres g " +
            "LEFT JOIN b.language l " +
            "WHERE (:genres IS NULL OR g.id IN :genres) " +
            "AND (:language IS NULL OR l.id = :language) " +
            "AND (:fiction IS NULL OR b.fiction = :fiction) " +
            "AND (:authorIds IS NULL OR a.id IN :authorIds) " +
            "AND (:pagesMin IS NULL OR b.pages >= :pagesMin) " +
            "AND (:pagesMax IS NULL OR b.pages <= :pagesMax) ")
            
    Page<Book> filter(
            @Param("genres") List<Long> genres,
            @Param("language") Long language,
            @Param("fiction") Boolean fiction,
            @Param("authorIds") List<Long> authorIds,
            @Param("pagesMin") Integer pagesMin,
            @Param("pagesMax") Integer pagesMax,
            Pageable pageable);
}
