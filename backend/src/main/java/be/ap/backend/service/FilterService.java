package be.ap.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for detecting inappropriate content in user-submitted text.
 */
@Service
public class FilterService {

    private List<String> badWords;

    /**
     * Loads the bad word list from {@code badwords.json} on the classpath.
     */
    @PostConstruct
    public void init() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new ClassPathResource("badwords.json").getInputStream());

        badWords = new ArrayList<>();
        root.properties().forEach(entry -> entry.getValue().forEach(word -> badWords.add(word.asText())));
    }

    /**
     * Returns {@code true} if the text contains a bad word as a whole word,
     * using a word-boundary check that handles accented characters.
     */
    public boolean containsBadWord(String text) {
        if (text == null || text.isBlank())
            return false;
        String lower = text.toLowerCase();
        return badWords.stream().anyMatch(badWord -> {
            String pattern = "(?<![a-zà-ÿ])" + java.util.regex.Pattern.quote(badWord) + "(?![a-zà-ÿ])";
            return Pattern.compile(pattern).matcher(lower).find();
        });
    }

    private static final Pattern URL_PATTERN = Pattern.compile(
            "((https?|ftp)://|www\\.)[^\\s]{2,}",
            Pattern.CASE_INSENSITIVE);

    /**
     * Returns {@code true} if the text contains a URL or web address.
     */
    public boolean containsUrl(String text) {
        if (text == null || text.isBlank())
            return false;
        return URL_PATTERN.matcher(text).find();
    }

}