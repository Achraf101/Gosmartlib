package be.ap.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import be.ap.backend.dto.CampusDTO;
import be.ap.backend.entity.Campus;
import be.ap.backend.entity.School;
import be.ap.backend.repository.CampusRepository;
import jakarta.persistence.EntityManager;

public class CampusServiceTest {
    @Mock
    private CampusRepository campusRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CampusService campusService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createCampus_shouldCreateCampusWithSchool() {

        // Arrange
        CampusDTO dto = new CampusDTO();
        dto.setName("Campus North");
        dto.setAdres("Main Street 1");
        dto.setBorrowLimit(5);
        dto.setSchoolId(1L);

        School school = new School();
        school.setId(1L);

        when(entityManager.find(School.class, 1L)).thenReturn(school);

        Campus savedCampus = new Campus();
        savedCampus.setName("Campus North");
        savedCampus.setAdres("Main Street 1");
        savedCampus.setBorrowLimit(5);
        savedCampus.setSchool(school);

        when(campusRepository.save(any(Campus.class))).thenReturn(savedCampus);

        // Act
        Campus result = campusService.createCampus(dto);

        // Assert
        assertEquals("Campus North", result.getName());
        assertEquals("Main Street 1", result.getAdres());
        assertEquals(5, result.getBorrowLimit());
        assertEquals(school, result.getSchool());

        verify(entityManager).find(School.class, 1L);
        verify(campusRepository).save(any(Campus.class));
    }

    @Test
    void createCampus_shouldCreateCampusWithoutSchool() {

        // Arrange
        CampusDTO dto = new CampusDTO();
        dto.setName("Campus South");
        dto.setAdres("Second Street 5");
        dto.setBorrowLimit(10);

        Campus savedCampus = new Campus();
        savedCampus.setName("Campus South");

        when(campusRepository.save(any(Campus.class))).thenReturn(savedCampus);

        // Act
        Campus result = campusService.createCampus(dto);

        // Assert
        assertEquals("Campus South", result.getName());

        verify(entityManager, never()).find(any(), any());
        verify(campusRepository).save(any(Campus.class));
    }
}
