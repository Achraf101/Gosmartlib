package be.ap.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.ap.backend.entity.School;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {

}