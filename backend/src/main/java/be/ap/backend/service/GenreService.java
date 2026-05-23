package be.ap.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import be.ap.backend.entity.Genre;
import be.ap.backend.repository.GenreRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    public List<Genre> getAll() {
        return genreRepository.findAll();
    }

    public List<Genre> search(String query) {
        return genreRepository.findByNameContainingIgnoreCase(query);
    }

    public Genre addGenre(Genre genre) {
        return genreRepository.save(genre);
    }
}