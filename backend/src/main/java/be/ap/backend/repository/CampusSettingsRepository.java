package be.ap.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import be.ap.backend.entity.CampusSettings;

public interface CampusSettingsRepository extends JpaRepository<CampusSettings, Long> {
    Optional<CampusSettings> findByCampusId(Long campusId);
}
