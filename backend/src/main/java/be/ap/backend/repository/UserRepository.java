package be.ap.backend.repository;

import be.ap.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.ssId = :ssId")
    Optional<User> findBySsId(@Param("ssId") String ssId);

    // User saveUser(User user);
}
