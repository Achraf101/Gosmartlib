package be.ap.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import be.ap.backend.entity.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    List<Author> findByName(String query);

    List<Author> findBy();
}
