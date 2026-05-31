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

/**
 * REST controller for managing locations within the system.
 * Access to location data is role-dependent (ADMIN vs library manager vs
 * others).
 */
@RestController
@RequestMapping("location")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;
    private final SessionContext sessionContext;

    /**
     * Creates a new location.
     *
     * @param dto location data transfer object
     * @return created location
     */
    @PostMapping
    public LocationDTO createLocation(@RequestBody LocationDTO dto) {
        return locationService.createLocation(dto);
    }

    /**
     * Retrieves all locations.
     *
     * <p>
     * Access rules:
     * - ADMIN: sees all locations
     * - BIBLIOTHEEKBEHEERDER: sees only locations of their school
     * - others: forbidden
     * </p>
     *
     * @return list of locations depending on role
     */
    @GetMapping
    public List<LocationDTO> getAll() {
        if (sessionContext.hasRole(UserRole.ADMIN)) {
            return locationService.findAll();
        } else if (sessionContext.hasRole(UserRole.BIBLIOTHEEKBEHEERDER)) {
            Long schoolId = sessionContext.getSchoolId();
            return locationService.getLocationsBySchool(schoolId);
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Niet toegelaten");
    }

    /**
     * Retrieves a location by its ID.
     *
     * @param id location id
     * @return location details
     */
    @GetMapping("/{id}")
    public LocationDTO getById(@PathVariable Long id) {
        return locationService.findById(id);
    }
}