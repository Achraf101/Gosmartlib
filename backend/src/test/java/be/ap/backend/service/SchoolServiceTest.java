package be.ap.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import be.ap.backend.entity.School;
import be.ap.backend.repository.SchoolRepository;

@ExtendWith(MockitoExtension.class)
public class SchoolServiceTest {
    @Mock
    private SchoolRepository schoolRepository;

    @InjectMocks
    private SchoolService schoolService;

    private School school;

    @BeforeEach
    void setUp() {
        school = new School();
        school.setName("Test School");
        school.setAdres("Test Street 1");
        school.setContact("info@testschool.be");
        school.setDescription("A test description");
    }

    @Test
    void addSchool_shouldSaveAndReturnSchool() {
        // Arrange
        when(schoolRepository.save(school)).thenReturn(school);

        // Act
        School savedSchool = schoolService.addSchool(school);

        // Assert
        assertNotNull(savedSchool);
        assertEquals("Test School", savedSchool.getName());

        verify(schoolRepository, times(1)).save(school);
        verifyNoMoreInteractions(schoolRepository);
    }
}
