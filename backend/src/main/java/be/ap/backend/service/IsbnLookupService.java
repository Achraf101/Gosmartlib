package be.ap.backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import be.ap.backend.dto.BookLookupDTO;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IsbnLookupService {
    @Value("${app.google-books.api-key}")
    private String apiKey;

    private final ObjectMapper objectMapper;

    public Optional<BookLookupDTO> lookup(String isbn) {
        try {
            String cleanIsbn = isbn.replaceAll("[^0-9Xx]", "");
            URL url = new URI("https://www.googleapis.com/books/v1/volumes?key=" + apiKey + "&q=isbn:" + cleanIsbn)
                    .toURL();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            if (connection.getResponseCode() != 200) {
                return Optional.empty();
            }

            String body;
            try (InputStream is = connection.getInputStream()) {
                body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            JsonNode root = objectMapper.readTree(body);

            if (root.path("totalItems").asInt(0) == 0) {
                return Optional.empty();
            }

            JsonNode volumeInfo = root.path("items").get(0).path("volumeInfo");

            BookLookupDTO dto = new BookLookupDTO();
            dto.setIsbn(isbn);

            String title = textOrNull(volumeInfo, "title");
            String subtitle = textOrNull(volumeInfo, "subtitle");
            if (title != null && subtitle != null && !subtitle.isBlank()) {
                title = title + ": " + subtitle;
            }
            dto.setTitle(title);

            dto.setPublisherName(textOrNull(volumeInfo, "publisher"));

            String rawDesc = textOrNull(volumeInfo, "description");
            if (rawDesc != null && rawDesc.length() > 500) {
                rawDesc = rawDesc.substring(0, 500);
            }
            dto.setDescription(rawDesc);

            String publishedDate = textOrNull(volumeInfo, "publishedDate");
            if (publishedDate != null && publishedDate.length() >= 4) {
                try {
                    dto.setPublishedYear(Integer.parseInt(publishedDate.substring(0, 4)));
                } catch (NumberFormatException ignored) {
                }
            }

            int pageCount = volumeInfo.path("pageCount").asInt(0);
            dto.setPages(pageCount > 0 ? pageCount : null);

            dto.setLanguageCode(textOrNull(volumeInfo, "language"));

            JsonNode authors = volumeInfo.path("authors");
            if (authors.isArray() && authors.size() > 0) {
                dto.setAuthorName(authors.get(0).asText(null));
                if (authors.size() > 1) {
                    List<String> contributors = new ArrayList<>();
                    for (int i = 1; i < authors.size(); i++) {
                        String name = authors.get(i).asText(null);
                        if (name != null)
                            contributors.add(name);
                    }
                    dto.setContributors(contributors);
                }
            }

            JsonNode imageLinks = volumeInfo.path("imageLinks");
            String thumbnail = imageLinks.path("thumbnail").asText(null);
            if (thumbnail != null) {
                thumbnail = thumbnail.replace("http://", "https://");
            }
            dto.setCoverUrl(thumbnail);

            return Optional.of(dto);

        } catch (IOException | URISyntaxException e) {
            return Optional.empty();
        }
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode child = node.path(field);
        return child.isMissingNode() || child.isNull() ? null : child.asText();
    }
}
