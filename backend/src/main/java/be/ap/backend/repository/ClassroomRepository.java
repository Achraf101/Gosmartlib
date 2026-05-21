package be.ap.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import be.ap.backend.entity.Classroom;
import be.ap.backend.entity.User;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    @Query("SELECT DISTINCT c FROM Classroom c LEFT JOIN FETCH c.students WHERE c.id = :id")
    Optional<Classroom> findByIdWithStudents(@Param("id") Long id);

    @Query("""
            SELECT CASE WHEN COUNT(s) > 0 THEN TRUE ELSE FALSE END
            FROM Enrollment e
            JOIN e.classroom c
            JOIN c.students s
            WHERE e.user.id = :teacherId
            AND e.role = 'LEERKRACHT'
            AND s.id = :studentId
            """)
    boolean teacherHasStudent(@Param("teacherId") Long teacherId, @Param("studentId") Long studentId);

    List<Classroom> findByStudentsContaining(User student);

    Optional<Classroom> findBySsId(String ssId);
}