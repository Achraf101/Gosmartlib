package be.ap.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.entity.Genre;
import be.ap.backend.repository.GenreRepository;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("genre")
public class GenreController {

    private final GenreRepository genreRepository;

    public GenreController(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @GetMapping()
    public List<Genre> getAll() {
        return genreRepository.findAll();
    }

    @GetMapping("search/{query}")
    public List<Genre> searchPublisher(@PathVariable String query) {
        return genreRepository.findByNameContainingIgnoreCase(query);
    }

    @PostMapping
    public Genre addPublisher(@RequestBody Genre genre) {
        return genreRepository.save(genre);
    }

}
