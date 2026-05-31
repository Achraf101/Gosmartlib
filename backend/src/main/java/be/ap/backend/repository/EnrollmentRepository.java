package be.ap.backend.repository;

import be.ap.backend.entity.Classroom;
import be.ap.backend.entity.Enrollment;
import be.ap.backend.entity.User;
import be.ap.backend.entity.UserRole;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByOneRosterId(String oneRosterId);

    /**
     * Returns all visible classrooms where the given user is enrolled as a teacher,
     * with enrollments eagerly fetched, ordered by name.
     */
    @Query("""
            SELECT DISTINCT e.classroom FROM Enrollment e
            LEFT JOIN FETCH e.classroom.enrollments
            WHERE e.user.id = :userId
            AND e.role = 'LEERKRACHT'
            AND e.classroom.hidden = false
            ORDER BY e.classroom.name ASC
            """)
    List<Classroom> findClassroomsByTeacherId(@Param("userId") Long userId);

    boolean existsByUserIdAndClassroomIdAndRole(Long userId, Long classroomId, UserRole role);

    boolean existsByUserIdAndClassroomId(Long userId, Long classroomId);

    /**
     * Returns all students enrolled in the given classroom.
     */
    @Query("SELECT e.user FROM Enrollment e WHERE e.classroom.id = :classroomId AND e.role = 'STUDENT'")
    List<User> findStudentsByClassroomId(@Param("classroomId") Long classroomId);

}