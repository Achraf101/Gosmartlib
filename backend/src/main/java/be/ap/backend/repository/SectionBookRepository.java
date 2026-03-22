package be.ap.backend.repository;

import be.ap.backend.entity.SectionBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionBookRepository extends JpaRepository<SectionBook, Long> {
    List<SectionBook> findBySectionId(Long sectionId);
    Optional<SectionBook> findBySectionIdAndGrade(Long sectionId, Byte grade);
}