package be.ap.backend.seeder;

import be.ap.backend.entity.*;
import be.ap.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final PublisherRepository publisherRepository;
    private final LanguageRepository languageRepository;

    public DataSeeder(BookRepository bookRepository,
                      AuthorRepository authorRepository,
                      GenreRepository genreRepository,
                      PublisherRepository publisherRepository,
                      LanguageRepository languageRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.publisherRepository = publisherRepository;
        this.languageRepository = languageRepository;
    }

    @Override
    public void run(String... args) {
        // Alleen seeden als er nog geen boeken zijn
        if (bookRepository.count() > 0) return;

        // --- Talen ---
        Language nl = new Language(); nl.setName("Nederlands"); nl.setCode("nl");
        Language en = new Language(); en.setName("Engels"); en.setCode("en");
        languageRepository.save(nl);
        languageRepository.save(en);

        // --- Uitgevers ---
        Publisher lannoo = new Publisher(); lannoo.setName("Lannoo");
        Publisher abrams = new Publisher(); abrams.setName("Abrams Books");
        Publisher bloom  = new Publisher(); bloom.setName("Bloomsbury");
        publisherRepository.save(lannoo);
        publisherRepository.save(abrams);
        publisherRepository.save(bloom);

        // --- Auteurs ---
        Author rindert = new Author(); rindert.setName("Rindert Kromhout");
        Author kinney  = new Author(); kinney.setName("Jeff Kinney");
        Author wiggs   = new Author(); wiggs.setName("Susan Wiggs");
        Author rowling = new Author(); rowling.setName("J.K. Rowling");
        authorRepository.save(rindert);
        authorRepository.save(kinney);
        authorRepository.save(wiggs);
        authorRepository.save(rowling);

        // --- Genres ---
        Genre jeugd   = new Genre(); jeugd.setName("Jeugd");
        Genre humor   = new Genre(); humor.setName("Humor");
        Genre roman   = new Genre(); roman.setName("Roman");
        Genre fantasy = new Genre(); fantasy.setName("Fantasy");
        genreRepository.save(jeugd);
        genreRepository.save(humor);
        genreRepository.save(roman);
        genreRepository.save(fantasy);

        // --- Boeken ---
        Book b1 = new Book();
        b1.setTitle("Het TikTok Kamp");
        b1.setIsbn("9789401480123");
        b1.setDescription("Een grappig verhaal over jongeren en sociale media op kamp.");
        b1.setFiction(true);
        b1.setPages(180);
        b1.setRating(4);
        b1.setRatingCount(120);
        b1.setAgeStart((byte) 10);
        b1.setAgeEnd((byte) 14);
        b1.setPublisherId(lannoo.getId());
        b1.setLanguageId(nl.getId());
        b1.setBookAuthorId(rindert.getId());
        bookRepository.save(b1);

        Book b2 = new Book();
        b2.setTitle("Het Leven Van Een Loser");
        b2.setIsbn("9780810993136");
        b2.setDescription("Het dagboek van de onhandige Greg Heffley op de middelbare school.");
        b2.setFiction(true);
        b2.setPages(217);
        b2.setRating(5);
        b2.setRatingCount(850);
        b2.setAgeStart((byte) 9);
        b2.setAgeEnd((byte) 13);
        b2.setPublisherId(abrams.getId());
        b2.setLanguageId(nl.getId());
        b2.setBookAuthorId(kinney.getId());
        bookRepository.save(b2);

        Book b3 = new Book();
        b3.setTitle("Romantische Kerst");
        b3.setIsbn("9780778328957");
        b3.setDescription("Een ontroerend kerstverhaal over liefde en tweede kansen.");
        b3.setFiction(true);
        b3.setPages(384);
        b3.setRating(4);
        b3.setRatingCount(310);
        b3.setAgeStart((byte) 16);
        b3.setAgeEnd((byte) 99);
        b3.setPublisherId(lannoo.getId());
        b3.setLanguageId(nl.getId());
        b3.setBookAuthorId(wiggs.getId());
        bookRepository.save(b3);

        Book b4 = new Book();
        b4.setTitle("Harry Potter And The Goblet Of Fire");
        b4.setIsbn("9780439139595");
        b4.setDescription("Harry's vierde jaar op Zweinstein met het gevaarlijke Toverschool Toernooi.");
        b4.setFiction(true);
        b4.setPages(636);
        b4.setRating(5);
        b4.setRatingCount(2400);
        b4.setAgeStart((byte) 11);
        b4.setAgeEnd((byte) 99);
        b4.setPublisherId(bloom.getId());
        b4.setLanguageId(en.getId());
        b4.setBookAuthorId(rowling.getId());
        bookRepository.save(b4);
    }
}
