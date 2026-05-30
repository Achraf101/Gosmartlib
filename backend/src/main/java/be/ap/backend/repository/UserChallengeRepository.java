package be.ap.backend.repository;

import be.ap.backend.entity.UserChallenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {
    List<UserChallenge> findByUserIdAndMonth(Long userId, String month);
    boolean existsByUserIdAndMonth(Long userId, String month);
    boolean existsByUserIdAndMonthAndChallengeId(Long userId, String month, Long challengeId);
}