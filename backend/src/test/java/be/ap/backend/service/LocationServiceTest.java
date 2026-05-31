package be.ap.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.ap.backend.dto.LocationDTO;
import be.ap.backend.entity.Location;
import be.ap.backend.entity.School;
import be.ap.backend.exception.MissingArgumentsException;
import be.ap.backend.repository.LocationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private LocationService locationService;

    private School school;
    private Location location;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1L);
        school.setBorrowLimit(5);
        school.setBorrowPeriod(14);
        school.setExtendLimit(3);
        school.setExtendPeriod(7);

        location = new Location();
        location.setId(1L);
        location.setName("Campus Noord");
        location.setAdres("Hoofdstraat 1");

        location.setSchool(school);
    }

    // ── createLocation ──────────────────────────────────────────────

    @Test
    void createLocation_success() {
        LocationDTO dto = validDto();

        when(entityManager.find(School.class, 1L)).thenReturn(school);
        when(locationRepository.save(any(Location.class))).thenReturn(location);

        LocationDTO result = locationService.createLocation(dto);

        assertThat(result.getName()).isEqualTo("Campus Noord");
        assertThat(result.getAdres()).isEqualTo("Hoofdstraat 1");
        assertThat(result.getSchoolId()).isEqualTo(1L);
        verify(entityManager).find(School.class, 1L);
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void createLocation_missingSchoolId_throwsMissingArgumentsException() {
        LocationDTO dto = validDto();
        dto.setSchoolId(null);

        assertThatThrownBy(() -> locationService.createLocation(dto))
                .isInstanceOf(MissingArgumentsException.class)
                .hasMessageContaining("School is verplicht");

        verify(locationRepository, never()).save(any());
    }

    // ── findById ──────────────────────────────────────────────────

    @Test
    void findById_exists_returnsDTO() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        LocationDTO result = locationService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Campus Noord");
        assertThat(result.getSchoolId()).isEqualTo(1L);
    }

    @Test
    void findById_notFound_throwsException() {
        when(locationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> locationService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // ── findAll ───────────────────────────────────────────────────

    @Test
    void findAll_returnsListOfDTOs() {
        when(locationRepository.findAll()).thenReturn(List.of(location));

        List<LocationDTO> result = locationService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Campus Noord");
    }

    @Test
    void findAll_empty_returnsEmptyList() {
        when(locationRepository.findAll()).thenReturn(List.of());

        List<LocationDTO> result = locationService.findAll();

        assertThat(result).isEmpty();
    }

    // ── helper ────────────────────────────────────────────────────

    private LocationDTO validDto() {
        LocationDTO dto = new LocationDTO();
        dto.setName("Campus Noord");
        dto.setAdres("Hoofdstraat 1");
        dto.setSchoolId(1L);
        return dto;
    }
}