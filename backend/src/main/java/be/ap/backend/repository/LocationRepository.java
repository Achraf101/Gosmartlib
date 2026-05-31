package be.ap.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import be.ap.backend.entity.Location;
import be.ap.backend.entity.School;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findBySchool(School school);

    @Query("SELECT c FROM Location c WHERE c.school.id = :schoolId")
    List<Location> getBySchoolId(@Param("schoolId") Long schoolId);

    @Query("SELECT c FROM Location c WHERE c.school.id = :schoolId")
    List<Location> getLocationBySchoolId(@Param("schoolId") Long schoolId);

    /**
     * Returns only the IDs of all locations belonging to the given school.
     */
    @Query("SELECT l.id FROM Location l WHERE l.school.id = :schoolId")
    List<Long> findIdsBySchoolId(@Param("schoolId") Long schoolId);
}