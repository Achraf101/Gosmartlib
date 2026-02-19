package be.ap.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import be.ap.backend.entity.Hello;

public interface HelloRepository extends JpaRepository<Hello, Long> {
    List<Hello> findFirstByOrderByIdAsc();
}
