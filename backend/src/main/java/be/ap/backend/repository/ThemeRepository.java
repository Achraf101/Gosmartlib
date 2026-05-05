package be.ap.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import be.ap.backend.entity.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    Theme findByName(String name);
}
