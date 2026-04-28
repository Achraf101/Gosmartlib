package be.ap.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.ap.backend.entity.School;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
    Optional<School> findBySsSubdomain(String ssSubdomain);
}