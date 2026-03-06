package be.ap.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import be.ap.backend.entity.BookType;
import be.ap.backend.entity.Genre;
import be.ap.backend.entity.Hello;
import be.ap.backend.entity.Language;
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

    public BackendApplication(HelloRepository helloRepository, LanguageRepository languageRepository,
            BookTypeRepository bookTypeRepository, GenreRepository genreRepository) {
        this.helloRepository = helloRepository;
        this.languageRepository = languageRepository;
        this.bookTypeRepository = bookTypeRepository;
        this.genreRepository = genreRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Override
    public void run(String... args) {

        helloRepository.save(new Hello("Database en API werken."));

        languageRepository.save(new Language("Nederlands", "nl"));
        languageRepository.save(new Language("Frans", "fr"));
        languageRepository.save(new Language("Engels", "en"));
        languageRepository.save(new Language("Duits", "de"));

        bookTypeRepository.save(new BookType("Boek"));
        bookTypeRepository.save(new BookType("Stripboek"));
        bookTypeRepository.save(new BookType("Magazine"));
        bookTypeRepository.save(new BookType("E-Boek"));

        genreRepository.save(new Genre("Roman"));
        genreRepository.save(new Genre("Avontuur"));
        genreRepository.save(new Genre("Biografie"));
        genreRepository.save(new Genre("Fantasie"));
    }
}
