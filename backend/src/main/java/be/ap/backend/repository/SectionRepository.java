package be.ap.backend.repository;

import be.ap.backend.entity.Section;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByHiddenFalseOrderByRankingAsc();
}