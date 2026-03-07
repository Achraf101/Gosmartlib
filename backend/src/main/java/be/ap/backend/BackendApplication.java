package be.ap.backend;

import java.time.Year;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookType;
import be.ap.backend.entity.Genre;
import be.ap.backend.entity.Hello;
import be.ap.backend.entity.Language;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.BookTypeRepository;
import be.ap.backend.repository.GenreRepository;
import be.ap.backend.repository.HelloRepository;
import be.ap.backend.repository.LanguageRepository;

@SpringBootApplication
public class BackendApplication implements CommandLineRunner {

    private final GenreRepository genreRepository;
    private final HelloRepository helloRepository;
    private final LanguageRepository languageRepository;
    private final BookTypeRepository bookTypeRepository;
    private final BookRepository bookRepository;

    public BackendApplication(HelloRepository helloRepository, LanguageRepository languageRepository,
            BookTypeRepository bookTypeRepository, GenreRepository genreRepository,
            BookRepository bookRepository) {
        this.helloRepository = helloRepository;
        this.languageRepository = languageRepository;
        this.bookTypeRepository = bookTypeRepository;
        this.genreRepository = genreRepository;
        this.bookRepository = bookRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Override
    public void run(String... args) {

        helloRepository.save(new Hello("Database en API werken."));

        Language nl = languageRepository.save(new Language("Nederlands", "nl"));
        languageRepository.save(new Language("Frans", "fr"));
        languageRepository.save(new Language("Engels", "en"));
        languageRepository.save(new Language("Duits", "de"));

        BookType boek = bookTypeRepository.save(new BookType("Boek"));
        bookTypeRepository.save(new BookType("Stripboek"));
        bookTypeRepository.save(new BookType("Magazine"));
        bookTypeRepository.save(new BookType("E-Boek"));

        genreRepository.save(new Genre("Roman"));
        genreRepository.save(new Genre("Avontuur"));
        genreRepository.save(new Genre("Biografie"));
        genreRepository.save(new Genre("Fantasie"));

        if (bookRepository.count() == 0) {
            bookRepository.save(createSampleBook("Jip en Janneke",
                    "De avonturen van twee kleuters die samen de wereld ontdekken.",
                    true, Year.of(1953), 160, boek, nl));
            bookRepository.save(createSampleBook("Pluk van de Petteflet",
                    "Pluk woont in de Petteflet en beleeft avonturen met zijn vrienden.",
                    true, Year.of(1971), 176, boek, nl));
            bookRepository.save(createSampleBook("Kruistocht in Spijkerbroek",
                    "Dolf reist terug in de tijd naar de Kinderkruistocht van 1212.",
                    true, Year.of(1973), 264, boek, nl));
            bookRepository.save(createSampleBook("Oorlogsgeheimen",
                    "Een jongen ontdekt geheimen uit de Tweede Wereldoorlog.",
                    true, Year.of(2002), 200, boek, nl));
            bookRepository.save(createSampleBook("Minoes",
                    "Een kat verandert in een vrouw en helpt een verlegen journalist.",
                    true, Year.of(1970), 176, boek, nl));
            bookRepository.save(createSampleBook("De Griezelbus",
                    "Onnoval schrijft enge verhalen die echt lijken te worden.",
                    true, Year.of(1988), 144, boek, nl));
        }
    }

    private Book createSampleBook(String title, String description, boolean fiction,
            Year published, int pages, BookType bookType, Language language) {
        Book book = new Book();
        book.setTitle(title);
        book.setDescription(description);
        book.setFiction(fiction);
        book.setPublished(published);
        book.setPages(pages);
        book.setBookType(bookType);
        book.setLanguage(language);
        return book;
    }
}
