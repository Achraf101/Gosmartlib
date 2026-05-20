package be.ap.backend.service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OpenLibraryService {

    private final ObjectMapper objectMapper;

    public Optional<String> getIaIdentifier(String isbn) {
        try {
            String body = fetchResponse(isbn);
            if (body == null) return Optional.empty();

            JsonNode root = objectMapper.readTree(body);
            JsonNode docs = root.path("docs");
            if (!docs.isArray() || docs.isEmpty()) {
                return Optional.empty();
            }

            JsonNode ia = docs.get(0).path("ia");
            if (!ia.isArray() || ia.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(ia.get(0).asText());

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    protected String fetchResponse(String isbn) throws Exception {
        URL url = new URI("https://openlibrary.org/search.json?isbn=" + isbn + "&fields=ia&limit=1")
                .toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "GoSmartLib/1.0");

        if (connection.getResponseCode() != 200) {
            return null;
        }

        try (InputStream is = connection.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
