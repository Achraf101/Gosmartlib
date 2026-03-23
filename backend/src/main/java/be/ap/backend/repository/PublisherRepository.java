package be.ap.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import be.ap.backend.entity.Publisher;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {

    List<Publisher> findByName(String query);

}
