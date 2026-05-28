package be.ap.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import be.ap.backend.config.SessionContext;
import be.ap.backend.dto.LocationDTO;
import be.ap.backend.entity.UserRole;
import be.ap.backend.service.LocationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;
    private final SessionContext sessionContext;


    @PostMapping
    public LocationDTO createLocation(@RequestBody LocationDTO dto) {
        return locationService.createLocation(dto);
    }

    @GetMapping
    public List<LocationDTO> getAll() {
        if (sessionContext.hasRole(UserRole.ADMIN)) {
            return locationService.findAll();
        }
        Long schoolId = sessionContext.getSchoolId();
        if (schoolId != null) {
            return locationService.getLocationsBySchool(schoolId);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Niet toegelaten");
    }

    @GetMapping("/{id}")
    public LocationDTO getById(@PathVariable Long id) {
        return locationService.findById(id);
    }
}