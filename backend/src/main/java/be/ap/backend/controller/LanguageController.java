package be.ap.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.entity.Language;
import be.ap.backend.repository.LanguageRepository;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("language")
public class LanguageController {

    private final LanguageRepository languageRepository;

    public LanguageController(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    @GetMapping()
    public List<Language> getAll() {
        return languageRepository.findAll();
    }

    @GetMapping("search/{query}")
    public List<Language> searchPublisher(@PathVariable String query) {
        return languageRepository.findByName(query);
    }

    @PostMapping
    public Language addPublisher(@RequestBody Language language) {
        return languageRepository.save(language);
    }

}
