package be.ap.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FilterServiceTest {

    private FilterService filterService;

    @BeforeEach
    void setUp() throws Exception {
        filterService = new FilterService();
        filterService.init();
    }

    @Test
    void givenTextWithBadWord_whenContainsBadWord_thenReturnTrue() {
        assertTrue(filterService.containsBadWord("Dit boek is echt kut."));
    }

    @Test
    void givenCleanText_whenContainsBadWord_thenReturnFalse() {
        assertFalse(filterService.containsBadWord("Dit is een geweldig boek!"));
    }

    @Test
    void givenNullText_whenContainsBadWord_thenReturnFalse() {
        assertFalse(filterService.containsBadWord(null));
    }

    @Test
    void givenBlankText_whenContainsBadWord_thenReturnFalse() {
        assertFalse(filterService.containsBadWord("   "));
    }

    @Test
    void givenTextWithUrl_whenContainsUrl_thenReturnTrue() {
        assertTrue(filterService.containsUrl("Kijk op www.spam.com voor meer info."));
    }

    @Test
    void givenTextWithHttpsUrl_whenContainsUrl_thenReturnTrue() {
        assertTrue(filterService.containsUrl("Ga naar https://example.com"));
    }

    @Test
    void givenCleanText_whenContainsUrl_thenReturnFalse() {
        assertFalse(filterService.containsUrl("Dit is een geweldige recensie."));
    }

    @Test
    void givenNullText_whenContainsUrl_thenReturnFalse() {
        assertFalse(filterService.containsUrl(null));
    }

    @Test
    void givenBlankText_whenContainsUrl_thenReturnFalse() {
        assertFalse(filterService.containsUrl("   "));
    }
}