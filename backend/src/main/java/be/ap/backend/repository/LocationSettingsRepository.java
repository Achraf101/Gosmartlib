package be.ap.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import be.ap.backend.entity.LocationSettings;

public interface LocationSettingsRepository extends JpaRepository<LocationSettings, Long> {
    Optional<LocationSettings> findByLocationId(Long locationId);
}
