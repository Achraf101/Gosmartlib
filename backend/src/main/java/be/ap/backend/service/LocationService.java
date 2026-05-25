package be.ap.backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import be.ap.backend.dto.LocationDTO;
import be.ap.backend.entity.Location;
import be.ap.backend.entity.School;
import be.ap.backend.exception.MissingArgumentsException;
import be.ap.backend.repository.LocationRepository;
import be.ap.backend.repository.SchoolRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final SchoolRepository schoolRepository;

    private final EntityManager entityManager;

    public LocationDTO createLocation(LocationDTO dto) {
        if (dto.getSchoolId() == null) {
            throw new MissingArgumentsException("School is verplicht!");
        }

        Location location = new Location();
        location.setName(dto.getName());
        location.setAdres(dto.getAdres());

        if (dto.getSchoolId() != null) {
            location.setSchool(entityManager.find(School.class, dto.getSchoolId()));
        }
        return toDTO(locationRepository.save(location));
    }

    public LocationDTO findById(Long id) {
        Location location = locationRepository.findById(id).orElseThrow();
        return toDTO(location);
    }

    public List<LocationDTO> findAll() {
        return locationRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    private LocationDTO toDTO(Location location) {
        LocationDTO dto = new LocationDTO();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setAdres(location.getAdres());
        if (location.getSchool() != null) {
            dto.setSchoolId(location.getSchool().getId());
        }
        return dto;
    }

    public List<LocationDTO> getLocationsBySchool(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "School not found"));

        return locationRepository.findBySchool(school)
                .stream()
                .map(this::toDTO)
                .toList();
    }
}
