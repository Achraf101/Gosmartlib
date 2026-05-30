package be.ap.backend.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import be.ap.backend.entity.Book;

import jakarta.annotation.PostConstruct;

@Service
public class EmailTemplateService {

    private String loanTemplate;
    private String reminderTemplate;
    private String bookRowTemplate;

    @Value("${app.domain}")
    private String domain; // needed for images in mail

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("nl", "BE"));

    @PostConstruct
    public void loadTemplates() throws IOException {
        loanTemplate = loadFile("templates/loan-overview.html");
        reminderTemplate = loadFile("templates/loan-reminder.html");
        bookRowTemplate = loadFile("templates/book-row.html");
    }

    private String loadFile(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public String buildLoanEmail(List<Book> books, LocalDate returnDate) {

        String formattedDate = returnDate.format(formatter);
        String template = loanTemplate;

        // make row for each book
        String bookRows = buildBookRows(books);

        template  = template.replace("{{book_html_rows}}", bookRows).replace("{{return_date}}", formattedDate);

        return template;
    }

    public String buildReminderEmail(List<Book> books, LocalDate returnDate) {

        String formattedDate = returnDate.format(formatter);
        String template = reminderTemplate;

        // make row for each book
        String bookRows = buildBookRows(books);

        template = template.replace("{{book_html_rows}}", bookRows).replace("{{return_date}}", formattedDate);

        return template;
    }

    private String buildBookRows(List<Book> books) {

        String bookRows = "";

        for (Book book : books) {
            bookRows += fillTemplate(bookRowTemplate, Map.of(
                    "cover_url",
                    (book.getCover().isBlank()) ? "https://" + domain
                            + "/assets/no-cover.svg"
                            : "https://" + domain + "/static/cover/" + book
                                    .getCover(),
                    "book_title", book.getTitle(),
                    "book_author", book.getAuthor().getName() // TODO add amount
            ));
        }

        return bookRows;
    }

    private String fillTemplate(String template, Map<String, String> values) {
        String result = template;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }
}