package be.ap.backend.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import be.ap.backend.entity.School;
import be.ap.backend.service.SchoolService;

class SchoolControllerTest {

    @Mock
    private SchoolService schoolService;

    @InjectMocks
    private SchoolController schoolController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddSchool() {
        // Arrange
        School inputSchool = new School();
        inputSchool.setName("Test School");

        School savedSchool = new School();
        savedSchool.setId(1);
        savedSchool.setName("Test School");

        when(schoolService.addSchool(inputSchool)).thenReturn(savedSchool);

        // Act
        School result = schoolController.addSchool(inputSchool);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test School", result.getName());

        verify(schoolService, times(1)).addSchool(inputSchool);
    }
}