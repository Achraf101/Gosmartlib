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
        campus.setSchool(school);
    }

    @Test
    void createCampus_withSchool_success() {
        CampusDTO dto = new CampusDTO();
        dto.setName("Campus Noord");
        dto.setAdres("Hoofdstraat 1");
        dto.setBorrowLimit(5);
        dto.setSchoolId(1L);

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
    void createCampus_withoutSchool_success() {
        CampusDTO dto = new CampusDTO();
        dto.setName("Campus South");
        dto.setAdres("Second Street 5");
        dto.setBorrowLimit(10);

        Campus saved = new Campus();
        saved.setId(2L);
        saved.setName("Campus Zuid");
        saved.setAdres("Second Street 5");
        saved.setBorrowLimit(10);

        when(campusRepository.save(any(Campus.class))).thenReturn(saved);

        CampusDTO result = campusService.createCampus(dto);

        assertThat(result.getName()).isEqualTo("Campus Zuid");
        assertThat(result.getSchoolId()).isNull();
        verify(entityManager, never()).find(any(), any());
        verify(campusRepository).save(any(Campus.class));
    }

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
}