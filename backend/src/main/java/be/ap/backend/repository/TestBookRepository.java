package be.ap.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import be.ap.backend.entity.TestBook;

public interface TestBookRepository extends JpaRepository<TestBook, Long> {
}