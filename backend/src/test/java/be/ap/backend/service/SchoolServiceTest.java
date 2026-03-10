package be.ap.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void addSchool_shouldSaveAndReturnSchool() {
        // Arrange
        School school = new School();
        school.setId(1L);
        school.setName("AP hogeschool");

        when(schoolRepository.save(school)).thenReturn(school);

        // Act
        School result = schoolService.addSchool(school);

        // Assert
        assertEquals("AP hogeschool", result.getName());
        verify(schoolRepository).save(school);
    }

    @Test
    void getAll_shouldReturnAllSchools() {
        // Arrange
        School school1 = new School();
        school1.setName("School A");

        School school2 = new School();
        school2.setName("School B");

        when(schoolRepository.findAll()).thenReturn(List.of(school1, school2));

        // Act
        List<School> result = schoolService.getAll();

        // Assert
        assertEquals(2, result.size());
        verify(schoolRepository).findAll();
    }

    @Test
    void findById_shouldReturnSchoolWhenExists() {
        // Arrange
        School school = new School();
        school.setId(1L);
        school.setName("AP hogeschool");

        when(schoolRepository.findById(1L)).thenReturn(Optional.of(school));

        // Act
        Optional<School> result = schoolService.findById(1L);

        // Assert
        assertEquals(true, result.isPresent());
        assertEquals("AP hogeschool", result.get().getName());
        verify(schoolRepository).findById(1L);
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        // Arrange
        when(schoolRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<School> result = schoolService.findById(1L);

        // Assert
        assertEquals(true, result.isEmpty());
        verify(schoolRepository).findById(1L);
    }
}
