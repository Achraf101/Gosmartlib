package be.ap.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import be.ap.backend.entity.Author;
import be.ap.backend.repository.AuthorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Service for managing authors.
 */
@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;

    public List<Author> getAll() {
        return authorRepository.findAll();
    }

    /**
     * @throws EntityNotFoundException if no author exists with the given ID
     */
    public Author getById(Long id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Auteur niet gevonden met id: " + id));
    }

    public List<Author> search(String query) {
        return authorRepository.searchByName(query);
    }

    public Author addAuthor(Author author) {
        return authorRepository.save(author);
    }
}