package be.ap.backend.repository;

import be.ap.backend.entity.SectionBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SectionBookRepository extends JpaRepository<SectionBook, Long> {
    List<SectionBook> findBySectionId(Long sectionId);
}