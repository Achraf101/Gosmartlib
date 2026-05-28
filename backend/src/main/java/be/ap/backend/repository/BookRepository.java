package be.ap.backend.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import be.ap.backend.dto.BookCardDTO;
import be.ap.backend.dto.GenreProjectionDTO;
import be.ap.backend.dto.ThemeProjectionDTO;
import be.ap.backend.dto.BookResultDTO;
import be.ap.backend.entity.Book;
import be.ap.backend.enums.Clib;
import jakarta.transaction.Transactional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Page<Book> findAll(Pageable pageable);

    @Query("""
            SELECT DISTINCT lb.book FROM LocationBook lb
            WHERE lb.location.id IN :locationIds
            AND (:didactic IS NULL
            OR (:didactic = true AND lb.book.didactic = true)
            OR (:didactic = false AND (lb.book.didactic = false OR lb.book.didactic IS NULL)))
            """)
    Page<Book> findAllByLocationAndDidactic(@Param("locationIds") List<Long> locationIds,
            @Param("didactic") Boolean didactic,
            Pageable pageable);

    boolean existsByIsbn(String isbn);

    @Query("SELECT b.isbn FROM Book b WHERE b.isbn IS NOT NULL")
    Set<String> findAllIsbns();

    @Query("""
            SELECT DISTINCT new be.ap.backend.dto.BookCardDTO(
                b.id,
                b.title,
                b.cover,
                b.author
                )
            FROM Book b
            JOIN b.genres g
            JOIN b.locationBooks lb
            WHERE lb.location.id IN :locationIds
            AND g IN (
                SELECT g2
                FROM Book b2
                JOIN b2.genres g2
                WHERE b2.id = :id
            )
            AND b.id != :id
            """)
    List<BookCardDTO> findRelated(@Param("id") Long id, @Param("locationIds") List<Long> locationIds);

    @Query("""
            SELECT DISTINCT b FROM Book b
            LEFT JOIN b.author a
            LEFT JOIN b.themes t
            LEFT JOIN b.genres g
            LEFT JOIN b.series s
            JOIN b.locationBooks lb
            WHERE lb.location.id IN :locationIds
            AND (:didactic IS NULL
            OR (:didactic = true AND b.didactic = true)
            OR (:didactic = false AND (b.didactic = false OR b.didactic IS NULL)))
            AND (
            LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR b.isbn LIKE CONCAT('%', :query, '%')
            )""")
    Page<Book> search(@Param("locationIds") List<Long> locationIds, @Param("query") String query,
            @Param("didactic") Boolean didactic, Pageable pageable);

    @Query("""
            SELECT new be.ap.backend.dto.BookResultDTO(
                b.id,
                b.title,
                b.cover,
                a.name,
                bt,
                s.id,
                s.name,
                b.seriesNumber,
                l,
                b.published,
                b.description,
                b.fiction,
                b.clib,
                b.pages,
                0,
                0
            )
            FROM Book b
            LEFT JOIN b.author a
            LEFT JOIN b.bookType bt
            LEFT JOIN b.series s
            LEFT JOIN b.language l
            ORDER BY b.id ASC
            """)
    Page<BookResultDTO> getAllBookResults(Pageable pageable);

    @Query("""
            SELECT b.id as bookId, g.id as genreId, g.name as genreName
            FROM Book b
            JOIN b.genres g
            WHERE b.id IN :bookIds
            """)
    List<GenreProjectionDTO> findGenresForBooks(@Param("bookIds") List<Long> bookIds);

    @Query("""
            SELECT b.id as bookId, t.id as themeId, t.name as themeName
            From Book b
            Join b.themes t
            WHERE b.id IN :bookIds
            """)
    List<ThemeProjectionDTO> findThemesForBooks(@Param("bookIds") List<Long> bookIds);

    @Query(value = """
            SELECT DISTINCT b FROM Book b
            LEFT JOIN b.author a
            LEFT JOIN b.genres g
            LEFT JOIN b.language l
            LEFT JOIN b.series s
            LEFT JOIN b.themes t
            JOIN b.locationBooks lb
            WHERE (COALESCE(:locationIds, NULL) IS NULL OR lb.location.id IN :locationIds)
            AND (:genres IS NULL OR g.id IN :genres)
            AND (:language IS NULL OR l.id = :language)
            AND (:didactic IS NULL
                 OR (:didactic = true AND b.didactic = true)
                 OR (:didactic = false AND (b.didactic = false OR b.didactic IS NULL)))
            AND (:fiction IS NULL OR b.fiction = :fiction)
            AND (:authorIds IS NULL OR a.id IN :authorIds)
            AND (COALESCE(:seriesIds, NULL) IS NULL OR s.id IN :seriesIds)
            AND (:pagesMin IS NULL OR b.pages >= :pagesMin)
            AND (:pagesMax IS NULL OR b.pages <= :pagesMax)
            AND (COALESCE(:clibs, NULL) IS NULL OR b.clib IN :clibs)
            AND (:themes IS NULL OR t.id IN :themes)
            AND (:query IS NULL
                 OR LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR b.isbn LIKE CONCAT('%', :query, '%'))
            """)
    Page<Book> filter(
            @Param("locationIds") List<Long> locationIds,
            @Param("genres") List<Long> genres,
            @Param("language") Long language,
            @Param("fiction") Boolean fiction,
            @Param("authorIds") List<Long> authorIds,
            @Param("seriesIds") List<Long> seriesIds,
            @Param("pagesMin") Integer pagesMin,
            @Param("pagesMax") Integer pagesMax,
            @Param("clibs") List<Clib> clibs,
            @Param("themes") List<Long> themes,
            @Param("didactic") Boolean didactic,
            @Param("query") String query,
            Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Book b SET b.cover = :filename WHERE b.id = :id")
    int updateCover(@Param("id") Long id, @Param("filename") String filename);

    @Query("SELECT COUNT(lb) > 0 FROM LocationBook lb WHERE lb.book.id = :bookId AND lb.location.id IN :locationIds")
    boolean existsByIdAndLocationId(@Param("bookId") Long bookId, @Param("locationIds") List<Long> locationIds);
}