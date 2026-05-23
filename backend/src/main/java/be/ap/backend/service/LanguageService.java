package be.ap.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import be.ap.backend.entity.Language;
import be.ap.backend.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LanguageService {

    private final LanguageRepository languageRepository;

    public List<Language> getAll() {
        return languageRepository.findAll();
    }

    public List<Language> search(String query) {
        return languageRepository.findByName(query);
    }

    public Language addLanguage(Language language) {
        return languageRepository.save(language);
    }

    public Language findByCode(String code) {
        return languageRepository.findByCode(code);
    }
}