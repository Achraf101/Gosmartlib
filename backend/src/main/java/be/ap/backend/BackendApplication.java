package be.ap.backend;

import java.time.Year;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

@SpringBootApplication
public class BackendApplication implements CommandLineRunner {

    private final GenreRepository genreRepository;
    private final HelloRepository helloRepository;
    private final LanguageRepository languageRepository;
    private final BookTypeRepository bookTypeRepository;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final SectionRepository sectionRepository;
    private final SectionBookRepository sectionBookRepository;

    public BackendApplication(HelloRepository helloRepository, LanguageRepository languageRepository,
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

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Override
    public void run(String... args) {

        if (bookRepository.count() > 0) {
            if (sectionRepository.count() == 0) {
                seedSections();
            }
            return;
        }

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

        Author tonke = createAuthor("Tonke Dragt");
        Author thea = createAuthor("Thea Beckman");
        Author jk = createAuthor("J.K. Rowling");
        Author rima = createAuthor("Rima Orie");
        Author benno = createAuthor("Benno Barnard");

        Book b1 = bookRepository.save(createSampleBook("De brief voor de koning",
                "Vijf jongelingen moeten, voordat ze tot ridder geslagen worden, de nacht biddend en wakend doorbrengen. Eén van hen hoort een noodkreet van buiten en gaat op onderzoek uit. Met deze daad bewijst hij pas een echte ridder te zijn. ",
                true, Year.of(1962), 449, boek, nl, tonke,
                "https://webservices.bibliotheek.be/index.php?func=cover&ISBN=9789025873530&VLACCnr=10565678&CDR=&EAN=&ISMN=&EBS=&coversize=large"));
        Book b2 = bookRepository.save(createSampleBook("Harry Potter en de vuurbeker",
                "Als tovenaar-in-de-dop Harry Potter deelneemt aan een internationaal tovenaarstoernooi, dreigt er onverwacht gevaar. ",
                true, Year.of(2000), 546, boek, nl, jk,
                "https://webservices.bibliotheek.be/index.php?func=cover&ISBN=9789076174204&VLACCnr=10542926&CDR=&EAN=&ISMN=&EBS=&coversize=large"));
        Book b3 = bookRepository.save(createSampleBook("Kruistocht in Spijkerbroek",
                "Dolf Wega belandt door een tijdmachine plotseling in de kinderkruistocht van 1212. Omdat hij niet meer terug kan naar de 20e eeuw , besluit hij ongeveer 8000 kinderen te volgen op hun gevaarlijke tocht over de Alpen naar Genua, waar een wonder zal gebeuren. ",
                true, Year.of(1973), 264, boek, nl, thea,
                "https://webservices.bibliotheek.be/index.php?func=cover&ISBN=9789060691670&VLACCnr=10420689&CDR=&EAN=&ISMN=&EBS=&coversize=large"));
        Book b4 = bookRepository.save(createSampleBook("Geef me de ruimte!",
                "De lotgevallen van een Vlaams meisje dat van huis wegloopt en in het middeleeuwse Frankrijk een zwervend, avontuurlijk bestaan gaat leiden als vrouw van een vrijgevochten troubadour. ",
                true, Year.of(1976), 406, boek, nl, thea,
                "https://webservices.bibliotheek.be/index.php?func=cover&ISBN=9789056377298&VLACCnr=10559524&CDR=&EAN=&ISMN=&EBS=&coversize=large"));
        Book b5 = bookRepository.save(createSampleBook("De Zwendelprins",
                "Simran (17) werkt als keukenhulp in het paleis van de maharadja van Suryan als ze wordt ontvoerd door een mysterieuze prins uit het noordelijke Fengart. Al snel blijkt dat Simran niet zomaar kan terugkeren naar haar oude leven. Wat volgt is een groot avontuur dwars door de bergen en de woestijn, waarin Simran zichzelf en haar eigen cultuur beter leert kennen. ",
                true, Year.of(2019), 399, boek, nl, rima,
                "https://webservices.bibliotheek.be/index.php?func=cover&ISBN=9789048860333&VLACCnr=10313312&CDR=&EAN=&ISMN=&EBS=&coversize=large"));
        Book b6 = bookRepository.save(createSampleBook(
                "Een geschiedenis van België voornieuwsgierige kinderen (en hun ouders)",
                "Geschiedenis van Belgie͏̈ vanaf 1830 tot 2003 in hoofdlijnen. ",
                true, Year.of(2012), 319, boek, nl, benno,
                "https://webservices.bibliotheek.be/index.php?func=cover&ISBN=9789045048031&VLACCnr=10412649&CDR=&EAN=&ISMN=&EBS=&coversize=large"));

        seedSectionsWithBooks(b1, b2, b3, b4, b5);
    }

    private void seedSections() {
        
        java.util.List<Book> books = bookRepository.findAll().stream().limit(5).toList();
        if (books.size() < 2) return;

        Section inDeKijker = createSection("In de kijker", (byte) 0);
        Section boekVanDeMaand = createSection("Boek van de maand", (byte) 1);

        for (int i = 0; i < Math.min(4, books.size() - 1); i++) {
            saveSectionBook(inDeKijker, books.get(i), (short) i);
        }
        saveSectionBook(boekVanDeMaand, books.get(books.size() - 1), (short) 0);
    }

    private void seedSectionsWithBooks(Book b1, Book b2, Book b3, Book b4, Book b5) {
        Section inDeKijker = createSection("In de kijker", (byte) 0);
        Section boekVanDeMaand = createSection("Boek van de maand", (byte) 1);

        saveSectionBook(inDeKijker, b1, (short) 0);
        saveSectionBook(inDeKijker, b2, (short) 1);
        saveSectionBook(inDeKijker, b3, (short) 2);
        saveSectionBook(inDeKijker, b4, (short) 3);
        saveSectionBook(boekVanDeMaand, b5, (short) 0);
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

    private Author createAuthor(String name) {
        Author author = new Author();
        author.setName(name);
        return authorRepository.save(author);
    }

    private Book createSampleBook(String title, String description, boolean fiction,
            Year published, int pages, BookType bookType, Language language, Author author,
            String cover) {
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
        return book;
    }
}