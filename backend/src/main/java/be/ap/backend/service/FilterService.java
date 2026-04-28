package be.ap.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FilterService {

    private List<String> badWords;

    @PostConstruct
    public void init() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new ClassPathResource("badwords.json").getInputStream());

        badWords = new ArrayList<>();
        root.properties().forEach(entry -> entry.getValue().forEach(word -> badWords.add(word.asText())));
    }

    public boolean containsBadWord(String text) {
        if (text == null || text.isBlank())
            return false;
        String lower = text.toLowerCase();
        return badWords.stream().anyMatch(lower::contains);
    }

}