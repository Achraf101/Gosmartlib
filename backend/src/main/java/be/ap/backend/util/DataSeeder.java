package be.ap.backend.util;

import java.time.Year;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import be.ap.backend.entity.Author;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookType;
import be.ap.backend.entity.Genre;
import be.ap.backend.entity.Hello;
import be.ap.backend.entity.Language;
import be.ap.backend.entity.Section;
import be.ap.backend.entity.SectionBook;
import be.ap.backend.repository.AuthorRepository;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.BookTypeRepository;
import be.ap.backend.repository.GenreRepository;
import be.ap.backend.repository.HelloRepository;
import be.ap.backend.repository.LanguageRepository;
import be.ap.backend.repository.SectionBookRepository;
import be.ap.backend.repository.SectionRepository;
import org.springframework.beans.factory.annotation.Value;

@Component
public class DataSeeder implements CommandLineRunner {

    @Value("${app.seeding.enabled:true}")
    private boolean seedingEnabled;

    private final GenreRepository genreRepository;
    private final HelloRepository helloRepository;
    private final LanguageRepository languageRepository;
    private final BookTypeRepository bookTypeRepository;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final SectionRepository sectionRepository;
    private final SectionBookRepository sectionBookRepository;

    public DataSeeder(HelloRepository helloRepository, LanguageRepository languageRepository,
            BookTypeRepository bookTypeRepository, GenreRepository genreRepository,
            BookRepository bookRepository, AuthorRepository authorRepository,
            SectionRepository sectionRepository, SectionBookRepository sectionBookRepository) {
        this.helloRepository = helloRepository;
        this.languageRepository = languageRepository;
        this.bookTypeRepository = bookTypeRepository;
        this.genreRepository = genreRepository;
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.sectionRepository = sectionRepository;
        this.sectionBookRepository = sectionBookRepository;
    }

    @Override
    public void run(String... args) {
        if(!seedingEnabled) return;

        if (bookRepository.count() > 0) {
            if (sectionRepository.count() == 0) {
                seedSections();
            }
            return;
        }
        seedDatabase();
    }

    private void seedDatabase() {
        helloRepository.save(new Hello("Database en API werken."));
        seedLanguages();
        BookType boek = seedBookTypes();
        seedGenres();
        seedBooks(boek);
    }

    private void seedLanguages() {
        if(languageRepository.count() > 0) return;
        languageRepository.save(new Language("Nederlands", "nl"));
        languageRepository.save(new Language("Frans", "fr"));
        languageRepository.save(new Language("Engels", "en"));
        languageRepository.save(new Language("Duits", "de"));
    }

    private BookType seedBookTypes() {
        if(bookRepository.count() > 0) return bookTypeRepository.findByName("Boek");
        BookType boek = bookTypeRepository.save(new BookType("Boek"));
        bookTypeRepository.save(new BookType("Stripboek"));
        bookTypeRepository.save(new BookType("Magazine"));
        bookTypeRepository.save(new BookType("E-Boek"));
        return boek;
    }

    private void seedGenres() {
        if (genreRepository.count() > 0) return;

        // fictie
        genreRepository.save(new Genre("Literaire roman"));
        genreRepository.save(new Genre("Spanning / thriller"));
        genreRepository.save(new Genre("Detective / misdaad")); 
        genreRepository.save(new Genre("Fantasy"));
        genreRepository.save(new Genre("Sciencefiction"));
        genreRepository.save(new Genre("Dystopie"));
        genreRepository.save(new Genre("Historische roman"));
        genreRepository.save(new Genre("Romantiek"));
        genreRepository.save(new Genre("Coming-of-age"));
        genreRepository.save(new Genre("Avontuur"));
        genreRepository.save(new Genre("Oorlog & conflict"));
        genreRepository.save(new Genre("Horror"));
        genreRepository.save(new Genre("Humor"));
        genreRepository.save(new Genre("Graphic novel / strip"));
        genreRepository.save(new Genre("Poëzie"));

        // non-fictie
        genreRepository.save(new Genre("Biografie / autobiografie"));
        genreRepository.save(new Genre("Wetenschap & technologie"));
        genreRepository.save(new Genre("Filosofie"));
        genreRepository.save(new Genre("Maatschappij & politiek"));
        genreRepository.save(new Genre("Psychologie"));
        genreRepository.save(new Genre("Geschiedenis"));
        genreRepository.save(new Genre("Kunst & cultuur"));
    }

    private void seedBooks(BookType boek) {
        Language nl = languageRepository.findByCode("nl");

        Genre roman = genreRepository.findByName("Literaire roman");
        Genre avontuur = genreRepository.findByName("Avontuur");
        Genre fantasy = genreRepository.findByName("Fantasy");
        Genre geschiedenis = genreRepository.findByName("Geschiedenis");

        Author tonke = createAuthor("Tonke Dragt");
        Author thea = createAuthor("Thea Beckman");
        Author jk = createAuthor("J.K. Rowling");
        Author rima = createAuthor("Rima Orie");
        Author benno = createAuthor("Benno Barnard");

        Book b1 = saveBook("De brief voor de koning",
                "Vijf jongelingen moeten, voordat ze tot ridder geslagen worden, de nacht biddend en wakend doorbrengen.",
                true, Year.of(1962), 449, boek, nl, tonke,
                "eacc8ca9ee1827042fc7835e7de56227.webp",
                Set.of(roman, avontuur));

        Book b2 = saveBook("Harry Potter en de vuurbeker",
                "Als tovenaar-in-de-dop Harry Potter deelneemt aan een internationaal tovenaarstoernooi, dreigt er onverwacht gevaar.",
                true, Year.of(2000), 546, boek, nl, jk,
                "b983a2f49e023bb4e2b8c5370552c0f4.webp",
                Set.of(fantasy, avontuur));

        Book b3 = saveBook("Kruistocht in Spijkerbroek",
                "Dolf Wega belandt door een tijdmachine plotseling in de kinderkruistocht van 1212.",
                true, Year.of(1973), 264, boek, nl, thea,
                "5a6539e1224f4a55659a0136a0de562f.webp",
                Set.of(roman, avontuur));

        Book b4 = saveBook("Geef me de ruimte!",
                "De lotgevallen van een Vlaams meisje dat van huis wegloopt en in het middeleeuwse Frankrijk een zwervend bestaan gaat leiden.",
                true, Year.of(1976), 406, boek, nl, thea,
                "6353feab95305a1264c9430a565515de.webp",
                Set.of(roman, avontuur));

        Book b5 = saveBook("De Zwendelprins",
                "Simran (17) werkt als keukenhulp in het paleis van de maharadja van Suryan als ze wordt ontvoerd door een mysterieuze prins.",
                true, Year.of(2019), 399, boek, nl, rima,
                "811278f6a3909b01ed523a55f4b6b817.webp",
                Set.of(fantasy, avontuur));

        saveBook("Een geschiedenis van België voor nieuwsgierige kinderen",
                "Geschiedenis van België vanaf 1830 tot 2003 in hoofdlijnen.",
                false, Year.of(2012), 319, boek, nl, benno,
                "82f15cee848b29e0f9684f691f2f020e.webp",
                Set.of(geschiedenis));

        seedSectionsWithBooks(b1, b2, b3, b4, b5);
    }

    private void seedSections() {
        java.util.List<Book> books = bookRepository.findAll().stream().limit(5).toList();
        if (books.size() < 2) {
            System.out.println("Not enough books to seed sections.");
            return;
        }
        createSectionsFromBooks(books.subList(0, Math.min(4, books.size() - 1)), books.get(books.size() - 1));
    }

    private void seedSectionsWithBooks(Book b1, Book b2, Book b3, Book b4, Book b5) {
        createSectionsFromBooks(java.util.List.of(b1, b2, b3, b4), b5);
    }

    private void createSectionsFromBooks(java.util.List<Book> inDeKijkerBooks, Book boekVanDeMaand) {
        Section inDeKijker = createSection("In de kijker", (byte) 0);
        Section boekVanDeMaandSection = createSection("Boek van de maand", (byte) 1);

        for (int i = 0; i < inDeKijkerBooks.size(); i++) {
            saveSectionBook(inDeKijker, inDeKijkerBooks.get(i), (short) i);
        }
        saveSectionBook(boekVanDeMaandSection, boekVanDeMaand, (short) 0);
    }

    private Book saveBook(String title, String description, boolean fiction,
        Year published, int pages, BookType bookType, Language language,
        Author author, String cover, Set<Genre> genres) {
        Book book = new Book();
        book.setTitle(title);
        book.setDescription(description);
        book.setFiction(fiction);
        book.setPublished(published);
        book.setPages(pages);
        book.setBookType(bookType);
        book.setLanguage(language);
        book.setAuthor(author);
        book.setCover(cover);
        book.setGenres(genres);
        return bookRepository.save(book);
    }

    private Author createAuthor(String name) {
        Author author = new Author();
        author.setName(name);
        return authorRepository.save(author);
    }

    private Section createSection(String title, byte ranking) {
        Section section = new Section();
        section.setTitle(title);
        section.setRanking(ranking);
        section.setSchoolId(1L);
        section.setHidden(false);
        return sectionRepository.save(section);
    }

    private void saveSectionBook(Section section, Book book, short ranking) {
        SectionBook sb = new SectionBook();
        sb.setSection(section);
        sb.setBook(book);
        sb.setRanking(ranking);
        sectionBookRepository.save(sb);
    }
}