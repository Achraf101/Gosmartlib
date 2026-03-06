package be.ap.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.entity.Author;
import be.ap.backend.repository.AuthorRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("author")
public class AuthorController {

    private final AuthorRepository authorRepository;

    public AuthorController(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @GetMapping()
    public List<Author> getAll() {
        return authorRepository.findBy();
    }

    @GetMapping("search/{query}")
    public List<Author> searchAuthor(@PathVariable String query) {
        return authorRepository.findByName(query);
    }

    @PostMapping
    public Author addAuthor(@RequestBody Author author) {
        return authorRepository.save(author);
    }

}
