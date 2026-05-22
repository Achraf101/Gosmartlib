package be.ap.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class OpenLibraryServiceTest {

    private OpenLibraryService service;

    @BeforeEach
    void setup() {
        service = spy(new OpenLibraryService(new ObjectMapper()));
    }

    @Test
    void givenValidResponse_whenIaPresent_thenReturnFirstIdentifier() throws Exception {
        String json = """
                {"docs":[{"ia":["lordofrings00tolk_5","lordofrings00tolk_2"]}]}
                """;
        doReturn(json).when(service).fetchResponse("9780747532743");

        Optional<String> result = service.getIaIdentifier("9780747532743");

        assertTrue(result.isPresent());
        assertEquals("lordofrings00tolk_5", result.get());
    }

    @Test
    void givenMultipleIaEntries_whenGetIaIdentifier_thenReturnFirstOnly() throws Exception {
        String json = """
                {"docs":[{"ia":["first_identifier","second_identifier","third_identifier"]}]}
                """;
        doReturn(json).when(service).fetchResponse("9780451526538");

        Optional<String> result = service.getIaIdentifier("9780451526538");

        assertTrue(result.isPresent());
        assertEquals("first_identifier", result.get());
    }

    @Test
    void givenResponseWithNoIaField_whenGetIaIdentifier_thenReturnEmpty() throws Exception {
        String json = """
                {"docs":[{"title":"Some Book"}]}
                """;
        doReturn(json).when(service).fetchResponse("9780000000001");

        Optional<String> result = service.getIaIdentifier("9780000000001");

        assertFalse(result.isPresent());
    }

    @Test
    void givenResponseWithEmptyIaArray_whenGetIaIdentifier_thenReturnEmpty() throws Exception {
        String json = """
                {"docs":[{"ia":[]}]}
                """;
        doReturn(json).when(service).fetchResponse("9780000000002");

        Optional<String> result = service.getIaIdentifier("9780000000002");

        assertFalse(result.isPresent());
    }

    @Test
    void givenResponseWithEmptyDocs_whenGetIaIdentifier_thenReturnEmpty() throws Exception {
        String json = """
                {"docs":[]}
                """;
        doReturn(json).when(service).fetchResponse("9780000000003");

        Optional<String> result = service.getIaIdentifier("9780000000003");

        assertFalse(result.isPresent());
    }

    @Test
    void givenNonOkHttpResponse_whenGetIaIdentifier_thenReturnEmpty() throws Exception {
        doReturn(null).when(service).fetchResponse("9780000000004");

        Optional<String> result = service.getIaIdentifier("9780000000004");

        assertFalse(result.isPresent());
    }

    @Test
    void givenNetworkError_whenGetIaIdentifier_thenReturnEmpty() throws Exception {
        doThrow(new RuntimeException("Connection refused")).when(service).fetchResponse("9780000000005");

        Optional<String> result = service.getIaIdentifier("9780000000005");

        assertFalse(result.isPresent());
    }
}
