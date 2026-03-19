package be.ap.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import be.ap.backend.dto.SchoolDTO;
import be.ap.backend.entity.School;
import be.ap.backend.repository.SchoolRepository;

@ExtendWith(MockitoExtension.class)
public class SchoolServiceTest {

    @Mock
    private SchoolRepository schoolRepository;

    private SchoolService schoolService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        schoolService = new SchoolService(schoolRepository);
    }

    // Helper to build a valid SchoolDTO
    private SchoolDTO buildValidDTO(String name) {
        SchoolDTO dto = new SchoolDTO();
        dto.setName(name);
        dto.setAdres("Ellermanstraat 33");
        dto.setContact("info@ap.be");
        dto.setDescription("Een hogeschool in Antwerpen.");
        return dto;
    }

    // Helper to build a School entity (for mock returns)
    private School buildSchoolEntity(Long id, String name) {
        School school = new School();
        school.setId(id);
        school.setName(name);
        school.setAdres("Ellermanstraat 33");
        school.setContact("info@ap.be");
        school.setDescription("Een hogeschool in Antwerpen.");
        school.setCampuses(List.of());
        return school;
    }

    @Test
    void addSchool_shouldSaveAndReturnSchool() {
        SchoolDTO dto = buildValidDTO("AP hogeschool");
        School savedEntity = buildSchoolEntity(1L, "AP hogeschool");

        // The service builds a new School internally, so match with any(School.class)
        when(schoolRepository.save(any(School.class))).thenReturn(savedEntity);

        SchoolDTO result = schoolService.addSchool(dto);

        assertEquals("AP hogeschool", result.getName());
        verify(schoolRepository).save(any(School.class));
    }

    @Test
    void getAll_shouldReturnAllSchools() {
        School school1 = buildSchoolEntity(1L, "School A");
        School school2 = buildSchoolEntity(2L, "School B");

        when(schoolRepository.findAll()).thenReturn(List.of(school1, school2));

        List<SchoolDTO> result = schoolService.getAll();

        assertEquals(2, result.size());
        assertEquals("School A", result.get(0).getName());
        assertEquals("School B", result.get(1).getName());
        verify(schoolRepository).findAll();
    }

    @Test
    void findById_shouldReturnSchoolWhenExists() {
        School school = buildSchoolEntity(1L, "AP hogeschool");

        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));

        Optional<SchoolDTO> result = schoolService.findById(1L);

        assertEquals(true, result.isPresent());
        assertEquals("AP hogeschool", result.get().getName());
        verify(schoolRepository).findById(1L);
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        when(schoolRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<SchoolDTO> result = schoolService.findById(1L);

        assertEquals(true, result.isEmpty());
        verify(schoolRepository).findById(1L);
    }
}