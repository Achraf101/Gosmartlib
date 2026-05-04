package be.ap.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import be.ap.backend.entity.SavedList;
import jakarta.transaction.Transactional;

public interface SavedListRepository extends JpaRepository<SavedList, Long>{
    List<SavedList> findByUserId(Long userId);
    @Transactional
    void deleteByUserIdAndBookListId(Long userId, Long bookListId);
    boolean existsByUserIdAndBookListId(Long userId, Long bookListId);
    
    @Transactional
    void deleteByBookListId(Long bookListId);
}
