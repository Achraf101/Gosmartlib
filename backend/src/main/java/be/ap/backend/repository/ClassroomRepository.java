package be.ap.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import be.ap.backend.entity.Classroom;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    /**
     * Returns the classroom with the given ID, eagerly fetching its enrollments and
     * users.
     */
    @Query("SELECT DISTINCT c FROM Classroom c LEFT JOIN FETCH c.enrollments e LEFT JOIN FETCH e.user WHERE c.id = :id")
    Optional<Classroom> findByIdWithStudents(@Param("id") Long id);

    /**
     * Returns {@code true} if the given teacher and student share at least one
     * classroom.
     */
    @Query("""
            SELECT CASE WHEN COUNT(se) > 0 THEN TRUE ELSE FALSE END
            FROM Enrollment te
            JOIN Enrollment se ON se.classroom = te.classroom
            WHERE te.user.id = :teacherId
            AND te.role = 'LEERKRACHT'
            AND se.user.id = :studentId
            AND se.role = 'STUDENT'
            """)
    boolean teacherHasStudent(@Param("teacherId") Long teacherId, @Param("studentId") Long studentId);

    Optional<Classroom> findBySsId(String ssId);
}