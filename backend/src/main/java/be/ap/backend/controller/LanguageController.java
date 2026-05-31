package be.ap.backend.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import be.ap.backend.entity.Language;
import be.ap.backend.service.LanguageService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * REST controller for managing languages.
 * Provides endpoints to retrieve, search, create, and lookup languages by code.
 */
@RestController
@RequestMapping("language")
@RequiredArgsConstructor
public class LanguageController {

    private final LanguageService languageService;

    /**
     * Retrieves all available languages.
     *
     * @return list of all languages
     */
    @GetMapping
    public ResponseEntity<List<Language>> getAll() {
        return ResponseEntity.ok(languageService.getAll());
    }

    /**
     * Searches languages by a query string.
     *
     * @param query search term used to filter languages
     * @return list of matching languages
     */
    @GetMapping("/search/{query}")
    public ResponseEntity<List<Language>> search(@PathVariable String query) {
        return ResponseEntity.ok(languageService.search(query));
    }

    /**
     * Creates a new language entry.
     *
     * @param language language object to persist
     * @return the created language
     */
    @PostMapping
    public ResponseEntity<Language> addLanguage(@RequestBody Language language) {
        return ResponseEntity.ok(languageService.addLanguage(language));
    }

    /**
     * Finds a language by its ISO code.
     *
     * @param code language code (e.g. "en", "nl")
     * @return the matching language
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<Language> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(languageService.findByCode(code));
    }
}
