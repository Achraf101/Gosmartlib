package be.ap.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import be.ap.backend.entity.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    List<Author> findByName(String query);
    
    @Query("SELECT a FROM Author a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Author> searchByName(@Param("query") String query);
}
