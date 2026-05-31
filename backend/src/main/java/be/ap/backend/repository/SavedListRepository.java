package be.ap.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import be.ap.backend.entity.SavedList;
import jakarta.transaction.Transactional;

public interface SavedListRepository extends JpaRepository<SavedList, Long> {
    List<SavedList> findByUserId(Long userId);

    /**
     * Removes the saved list entry for the given user and book list.
     */
    @Transactional
    void deleteByUserIdAndBookListId(Long userId, Long bookListId);

    boolean existsByUserIdAndBookListId(Long userId, Long bookListId);

    /**
     * Removes all saved list entries for the given book list.
     */
    @Transactional
    void deleteByBookListId(Long bookListId);
}
