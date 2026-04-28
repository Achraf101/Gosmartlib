package be.ap.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.ap.backend.dto.CampusDTO;
import be.ap.backend.entity.Campus;
import be.ap.backend.entity.School;
import be.ap.backend.exception.ArgumentsInvalidException;
import be.ap.backend.exception.MissingArgumentsException;
import be.ap.backend.repository.CampusRepository;
import jakarta.persistence.EntityManager;

@ExtendWith(MockitoExtension.class)
public class CampusServiceTest {

    @Mock
    private CampusRepository campusRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CampusService campusService;

    private School school;
    private Campus campus;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setId(1L);

        campus = new Campus();
        campus.setId(1L);
        campus.setName("Campus Noord");
        campus.setAdres("Hoofdstraat 1");
        campus.setBorrowLimit(5);
        campus.setBorrowPeriod(14);
        campus.setExtendLimit(3);
        campus.setExtendPeriod(7);
        campus.setSchool(school);
    }

    // ── createCampus ──────────────────────────────────────────────

    @Test
    void createCampus_success() {
        CampusDTO dto = validDto();

        when(entityManager.find(School.class, 1L)).thenReturn(school);
        when(campusRepository.save(any(Campus.class))).thenReturn(campus);

        CampusDTO result = campusService.createCampus(dto);

        assertThat(result.getName()).isEqualTo("Campus Noord");
        assertThat(result.getAdres()).isEqualTo("Hoofdstraat 1");
        assertThat(result.getBorrowLimit()).isEqualTo(5);
        assertThat(result.getSchoolId()).isEqualTo(1L);
        verify(entityManager).find(School.class, 1L);
        verify(campusRepository).save(any(Campus.class));
    }

    @Test
    void createCampus_missingSchoolId_throwsMissingArgumentsException() {
        CampusDTO dto = validDto();
        dto.setSchoolId(null);

        assertThatThrownBy(() -> campusService.createCampus(dto))
                .isInstanceOf(MissingArgumentsException.class)
                .hasMessageContaining("School is verplicht");

        verify(campusRepository, never()).save(any());
    }

    @Test
    void createCampus_borrowLimitZero_throwsArgumentsInvalidException() {
        CampusDTO dto = validDto();
        dto.setBorrowLimit(0);

        assertThatThrownBy(() -> campusService.createCampus(dto))
                .isInstanceOf(ArgumentsInvalidException.class)
                .hasMessageContaining("minimaal 1");

        verify(campusRepository, never()).save(any());
    }

    @Test
    void createCampus_borrowPeriodZero_throwsArgumentsInvalidException() {
        CampusDTO dto = validDto();
        dto.setBorrowPeriod(0);

        assertThatThrownBy(() -> campusService.createCampus(dto))
                .isInstanceOf(ArgumentsInvalidException.class)
                .hasMessageContaining("minimaal 1");

        verify(campusRepository, never()).save(any());
    }

    @Test
    void createCampus_extendLimitZero_throwsArgumentsInvalidException() {
        CampusDTO dto = validDto();
        dto.setExtendLimit(0);

        assertThatThrownBy(() -> campusService.createCampus(dto))
                .isInstanceOf(ArgumentsInvalidException.class)
                .hasMessageContaining("minimaal 1");

        verify(campusRepository, never()).save(any());
    }

    @Test
    void createCampus_extendPeriodZero_throwsArgumentsInvalidException() {
        CampusDTO dto = validDto();
        dto.setExtendPeriod(0);

        assertThatThrownBy(() -> campusService.createCampus(dto))
                .isInstanceOf(ArgumentsInvalidException.class)
                .hasMessageContaining("minimaal 1");

        verify(campusRepository, never()).save(any());
    }

    @Test
    void createCampus_borrowLimitTooHigh_throwsArgumentsInvalidException() {
        CampusDTO dto = validDto();
        dto.setBorrowLimit(1000);

        assertThatThrownBy(() -> campusService.createCampus(dto))
                .isInstanceOf(ArgumentsInvalidException.class)
                .hasMessageContaining("999");

        verify(campusRepository, never()).save(any());
    }

    @Test
    void createCampus_borrowPeriodTooHigh_throwsArgumentsInvalidException() {
        CampusDTO dto = validDto();
        dto.setBorrowPeriod(1000);

        assertThatThrownBy(() -> campusService.createCampus(dto))
                .isInstanceOf(ArgumentsInvalidException.class)
                .hasMessageContaining("999");

        verify(campusRepository, never()).save(any());
    }

    @Test
    void createCampus_extendPeriodTooHigh_throwsArgumentsInvalidException() {
        CampusDTO dto = validDto();
        dto.setExtendPeriod(1000);

        assertThatThrownBy(() -> campusService.createCampus(dto))
                .isInstanceOf(ArgumentsInvalidException.class)
                .hasMessageContaining("999");

        verify(campusRepository, never()).save(any());
    }

    @Test
    void createCampus_extendLimitTooHigh_throwsArgumentsInvalidException() {
        CampusDTO dto = validDto();
        dto.setExtendLimit(11);

        assertThatThrownBy(() -> campusService.createCampus(dto))
                .isInstanceOf(ArgumentsInvalidException.class)
                .hasMessageContaining("10");

        verify(campusRepository, never()).save(any());
    }

    // ── findById ──────────────────────────────────────────────────

    @Test
    void findById_exists_returnsDTO() {
        when(campusRepository.findById(1L)).thenReturn(Optional.of(campus));

        CampusDTO result = campusService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Campus Noord");
        assertThat(result.getSchoolId()).isEqualTo(1L);
    }

    @Test
    void findById_notFound_throwsException() {
        when(campusRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> campusService.findById(99L))
                .isInstanceOf(NoSuchElementException.class);
    }

    // ── findAll ───────────────────────────────────────────────────

    @Test
    void findAll_returnsListOfDTOs() {
        when(campusRepository.findAll()).thenReturn(List.of(campus));

        List<CampusDTO> result = campusService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Campus Noord");
    }

    @Test
    void findAll_empty_returnsEmptyList() {
        when(campusRepository.findAll()).thenReturn(List.of());

        List<CampusDTO> result = campusService.findAll();

        assertThat(result).isEmpty();
    }

    // ── helper ────────────────────────────────────────────────────

    private CampusDTO validDto() {
        CampusDTO dto = new CampusDTO();
        dto.setName("Campus Noord");
        dto.setAdres("Hoofdstraat 1");
        dto.setSchoolId(1L);
        dto.setBorrowLimit(5);
        dto.setBorrowPeriod(14);
        dto.setExtendLimit(3);
        dto.setExtendPeriod(7);
        return dto;
    }
}