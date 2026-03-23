package be.ap.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


import be.ap.backend.entity.Language;

public interface LanguageRepository extends JpaRepository<Language, Long> {

    List<Language> findByName(String query);

    List<Language> findByCodeOrderById(String code);

    Language findByCode(String code);
}
