package be.ap.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import be.ap.backend.entity.Campus;
import be.ap.backend.entity.School;

import java.util.List;

@Repository
public interface CampusRepository extends JpaRepository<Campus, Long> {
    List<Campus> findBySchool(School school);

    @Query("SELECT c FROM Campus c WHERE c.school.id = :schoolId")
    List<Campus> getBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT c FROM Campus c WHERE c.school.id = :schoolId")
    List<Campus> getCampusBySchoolId(@Param("schoolId") Long schoolId);
}
