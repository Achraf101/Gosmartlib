package be.ap.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import be.ap.backend.entity.SchoolSettings;

public interface SchoolSettingsRepository extends JpaRepository<SchoolSettings, Long> {
    Optional<SchoolSettings> findBySchoolId(Long schoolId);
}
