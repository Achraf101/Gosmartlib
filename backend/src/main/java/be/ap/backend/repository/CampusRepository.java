package be.ap.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import be.ap.backend.entity.Campus;

@Repository
public interface CampusRepository extends JpaRepository<Campus, Long> {

}
