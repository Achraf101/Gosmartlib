package be.ap.backend.util;

import java.time.Year;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import be.ap.backend.entity.Author;
import be.ap.backend.entity.Book;
import be.ap.backend.entity.BookType;
import be.ap.backend.entity.Challenge;
import be.ap.backend.entity.Location;
import be.ap.backend.entity.Classroom;
import be.ap.backend.entity.Genre;
import be.ap.backend.entity.Language;
import be.ap.backend.entity.School;
import be.ap.backend.entity.Section;
import be.ap.backend.entity.SectionBook;
import be.ap.backend.entity.Theme;
import be.ap.backend.entity.User;
import be.ap.backend.entity.UserRole;
import be.ap.backend.repository.AuthorRepository;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.BookTypeRepository;
import be.ap.backend.repository.ChallengeRepository;
import be.ap.backend.repository.LocationRepository;
import be.ap.backend.repository.ClassroomRepository;
import be.ap.backend.repository.GenreRepository;
import be.ap.backend.repository.LanguageRepository;
import be.ap.backend.repository.SchoolRepository;
import be.ap.backend.repository.SectionBookRepository;
import be.ap.backend.repository.SectionRepository;
import be.ap.backend.repository.ThemeRepository;
import be.ap.backend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;

@Component

public class DataSeeder implements CommandLineRunner {

    @Value("${app.seeding.enabled:true}")
    private boolean seedingEnabled;

    @Value("${ADMIN_PASSWORD:admin}")
    private String adminPassword;

    private final GenreRepository genreRepository;
    private final LanguageRepository languageRepository;
    private final BookTypeRepository bookTypeRepository;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final SectionRepository sectionRepository;
    private final ChallengeRepository challengeRepository;
    private final SectionBookRepository sectionBookRepository;
    private final ThemeRepository themeRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final SchoolRepository schoolRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClassroomRepository classroomRepository;

    public DataSeeder(LanguageRepository languageRepository,
            BookTypeRepository bookTypeRepository, GenreRepository genreRepository,
            BookRepository bookRepository, AuthorRepository authorRepository,
            SectionRepository sectionRepository, SectionBookRepository sectionBookRepository,
            ThemeRepository themeRepository, UserRepository userRepository, LocationRepository locationRepository,
            SchoolRepository schoolRepository, PasswordEncoder passwordEncoder,
            ChallengeRepository challengeRepository, ClassroomRepository classroomRepository) {
        this.languageRepository = languageRepository;
        this.bookTypeRepository = bookTypeRepository;
        this.genreRepository = genreRepository;
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.sectionRepository = sectionRepository;
        this.sectionBookRepository = sectionBookRepository;
        this.challengeRepository = challengeRepository;
        this.themeRepository = themeRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.schoolRepository = schoolRepository;
        this.passwordEncoder = passwordEncoder;
        this.classroomRepository = classroomRepository;
    }

    @Override
    public void run(String... args) {
        if (!seedingEnabled)
            return;

        seedSchoolsAndLocations();
        seedChallenges();
        seedTestUsers();
        seedClassrooms();

        if (bookRepository.count() > 0) {
            if (sectionRepository.count() == 0) {
                seedSections();
            }
            return;
        }
        seedDatabase();

    }

    private void seedDatabase() {
        seedLanguages();
        BookType boek = seedBookTypes();
        seedGenres();
        seedThemes();
        seedBooks(boek);
    }

    private void seedLanguages() {
        if (languageRepository.count() > 0)
            return;
        languageRepository.save(new Language("Nederlands", "nl"));
        languageRepository.save(new Language("Frans", "fr"));
        languageRepository.save(new Language("Engels", "en"));
        languageRepository.save(new Language("Duits", "de"));
    }

    private BookType seedBookTypes() {
        if (bookRepository.count() > 0)
            return bookTypeRepository.findByName("Boek");
        BookType boek = bookTypeRepository.save(new BookType("Boek"));
        bookTypeRepository.save(new BookType("Stripboek"));
        bookTypeRepository.save(new BookType("Magazine"));
        bookTypeRepository.save(new BookType("E-Boek"));
        return boek;
    }

    private void seedGenres() {
        if (genreRepository.count() > 0)
            return;

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
        Theme avontuurEnOntdekking = themeRepository.findByName("Avontuur & ontdekking");
        Theme identiteitEnZelfbeeld = themeRepository.findByName("Identiteit & zelfbeeld");

        Author tonke = createAuthor("Tonke Dragt");
        Author thea = createAuthor("Thea Beckman");
        Author jk = createAuthor("J.K. Rowling");
        Author rima = createAuthor("Rima Orie");
        Author benno = createAuthor("Benno Barnard");

        Book b1 = saveBook("De brief voor de koning",
                "Vijf jongelingen moeten, voordat ze tot ridder geslagen worden, de nacht biddend en wakend doorbrengen.",
                true, Year.of(1962), 449, boek, nl, tonke,
                "eacc8ca9ee1827042fc7835e7de56227.webp",
                Set.of(roman, avontuur), Set.of(avontuurEnOntdekking));

        Book b2 = saveBook("Harry Potter en de vuurbeker",
                "Als tovenaar-in-de-dop Harry Potter deelneemt aan een internationaal tovenaarstoernooi, dreigt er onverwacht gevaar.",
                true, Year.of(2000), 546, boek, nl, jk,
                "b983a2f49e023bb4e2b8c5370552c0f4.webp",
                Set.of(fantasy, avontuur), Set.of(avontuurEnOntdekking));

        Book b3 = saveBook("Kruistocht in Spijkerbroek",
                "Dolf Wega belandt door een tijdmachine plotseling in de kinderkruistocht van 1212.",
                true, Year.of(1973), 264, boek, nl, thea,
                "5a6539e1224f4a55659a0136a0de562f.webp",
                Set.of(roman, avontuur), Set.of(avontuurEnOntdekking));

        Book b4 = saveBook("Geef me de ruimte!",
                "De lotgevallen van een Vlaams meisje dat van huis wegloopt en in het middeleeuwse Frankrijk een zwervend bestaan gaat leiden.",
                true, Year.of(1976), 406, boek, nl, thea,
                "6353feab95305a1264c9430a565515de.webp",
                Set.of(roman, avontuur), Set.of(identiteitEnZelfbeeld));

        Book b5 = saveBook("De Zwendelprins",
                "Simran (17) werkt als keukenhulp in het paleis van de maharadja van Suryan als ze wordt ontvoerd door een mysterieuze prins.",
                true, Year.of(2019), 399, boek, nl, rima,
                "811278f6a3909b01ed523a55f4b6b817.webp",
                Set.of(fantasy, avontuur), Set.of(avontuurEnOntdekking));

        saveBook("Een geschiedenis van België voor nieuwsgierige kinderen",
                "Geschiedenis van België vanaf 1830 tot 2003 in hoofdlijnen.",
                false, Year.of(2012), 319, boek, nl, benno,
                "82f15cee848b29e0f9684f691f2f020e.webp",
                Set.of(geschiedenis), Set.of());

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
            saveSectionBook(inDeKijker, inDeKijkerBooks.get(i), (short) (i + 1));
        }
        saveSectionBook(boekVanDeMaandSection, boekVanDeMaand, (short) 0);
    }

    private void seedChallenges() {
        if (challengeRepository.count() > 0)
            return;

        String[][] challenges = {
                { "Lees een Fantasy boek", "genre", "Fantasy" },
                { "Lees een Sciencefiction boek", "genre", "Sciencefiction" },
                { "Lees een Thriller boek", "genre", "Spanning / thriller" },
                { "Lees een Detective boek", "genre", "Detective / misdaad" },
                { "Lees een Dystopie boek", "genre", "Dystopie" },
                { "Lees een Historische roman", "genre", "Historische roman" },
                { "Lees een Romantiek boek", "genre", "Romantiek" },
                { "Lees een Coming-of-age boek", "genre", "Coming-of-age" },
                { "Lees een Avontuur boek", "genre", "Avontuur" },
                { "Lees een Oorlog & conflict boek", "genre", "Oorlog & conflict" },
                { "Lees een Horror boek", "genre", "Horror" },
                { "Lees een Humor boek", "genre", "Humor" },
                { "Lees een Graphic novel of strip", "genre", "Graphic novel / strip" },
                { "Lees een Poëzie boek", "genre", "Poëzie" },
                { "Lees een Biografie of autobiografie", "genre", "Biografie / autobiografie" },
                { "Lees een boek over Wetenschap & technologie", "genre", "Wetenschap & technologie" },
                { "Lees een Filosofie boek", "genre", "Filosofie" },
                { "Lees een boek over Maatschappij & politiek", "genre", "Maatschappij & politiek" },
                { "Lees een Psychologie boek", "genre", "Psychologie" },
                { "Lees een Geschiedenis boek", "genre", "Geschiedenis" },
                { "Lees een Kunst & cultuur boek", "genre", "Kunst & cultuur" },
                { "Lees een Literaire roman", "genre", "Literaire roman" },
                { "Lees een boek in het Frans", "language", "fr" },
                { "Lees een boek in het Engels", "language", "en" },
                { "Lees een boek in het Duits", "language", "de" },
                { "Lees een boek in het Nederlands", "language", "nl" },
                { "Lees een boek van meer dan 100 pagina's", "pages", "100" },
                { "Lees een boek van meer dan 200 pagina's", "pages", "200" },
                { "Lees een boek van meer dan 300 pagina's", "pages", "300" },
                { "Lees een boek van meer dan 400 pagina's", "pages", "400" },
                { "Lees een boek van meer dan 500 pagina's", "pages", "500" },
                { "Lees een boek van meer dan 150 pagina's", "pages", "150" },
                { "Lees een boek van meer dan 250 pagina's", "pages", "250" },
                { "Lees een boek van meer dan 350 pagina's", "pages", "350" },
                { "Lees een boek gepubliceerd voor 2000", "year", "2000" },
                { "Lees een boek gepubliceerd voor 1990", "year", "1990" },
                { "Lees een boek gepubliceerd voor 1980", "year", "1980" },
                { "Lees een boek gepubliceerd na 2010", "year", "2010" },
                { "Lees een boek gepubliceerd na 2015", "year", "2015" },
                { "Lees een boek gepubliceerd na 2018", "year", "2018" },
                { "Lees een boek gepubliceerd na 2020", "year", "2020" },
        };

        for (String[] c : challenges) {
            Challenge challenge = new Challenge();
            challenge.setDescription(c[0]);
            challenge.setConditionType(c[1]);
            challenge.setConditionValue(c[2]);
            challengeRepository.save(challenge);
        }
    }

    private Book saveBook(String title, String description, boolean fiction,
            Year published, int pages, BookType bookType, Language language,
            Author author, String cover, Set<Genre> genres, Set<Theme> themes) {
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
        book.setThemes(themes);
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

    private void seedThemes() {
        if (themeRepository.count() > 0)
            return;

        themeRepository.save(new Theme("Liefde & relaties"));
        themeRepository.save(new Theme("Vriendschap"));
        themeRepository.save(new Theme("Identiteit & zelfbeeld"));
        themeRepository.save(new Theme("Gender & seksualiteit"));
        themeRepository.save(new Theme("Diversiteit & inclusie"));
        themeRepository.save(new Theme("Mentale gezondheid"));
        themeRepository.save(new Theme("Rouw & verlies"));
        themeRepository.save(new Theme("Familie"));
        themeRepository.save(new Theme("School & prestatiedruk"));
        themeRepository.save(new Theme("Sociale media"));
        themeRepository.save(new Theme("Migratie & afkomst"));
        themeRepository.save(new Theme("Armoede & ongelijkheid"));
        themeRepository.save(new Theme("Macht & onrecht"));
        themeRepository.save(new Theme("Avontuur & ontdekking"));
        themeRepository.save(new Theme("Overleven"));
        themeRepository.save(new Theme("Toekomst & technologie"));
    }

    private void seedTestUsers() {
        Location location = locationRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Location 1 missing"));
        School school = schoolRepository.findById(1L).orElseThrow(() -> new IllegalStateException("School 1 missing"));

        seedUser("beheerder", "test1234", UserRole.BIBLIOTHEEKBEHEERDER, location, school);
        seedUser("admin", "admin", UserRole.ADMIN, location, school);
        seedUser("leerkracht1", "leerkracht1", UserRole.LEERKRACHT, location, school);
        seedUser("leerling1", "leerling1", UserRole.STUDENT, location, school);
        seedUser("leerling2", "leerling2", UserRole.STUDENT, location, school);
        seedUser("leerling3", "leerling3", UserRole.STUDENT, location, school);
    }

    private void seedUser(String username, String password, UserRole role, Location location, School school) {
        User user = userRepository.findByUsername(username).orElse(new User());
        if (user.getPassword() == null) {
            user.setPassword(passwordEncoder.encode(password));
        }
        user.setUsername(username);
        user.setRole(role);
        user.setLocation(location);
        user.setSchool(school);
        userRepository.save(user);
    }

    private void seedClassrooms() {
        if (classroomRepository.count() > 0)
            return;

        User teacher = userRepository.findByUsername("leerkracht1").orElse(null);
        User s1 = userRepository.findByUsername("leerling1").orElse(null);
        User s2 = userRepository.findByUsername("leerling2").orElse(null);
        User s3 = userRepository.findByUsername("leerling3").orElse(null);
        Location location = locationRepository.findById(1L).orElse(null);
        School school = schoolRepository.findById(1L).orElse(null);

        if (teacher == null)
            return;

        Classroom klas = new Classroom();
        klas.setName("3A");
        klas.setTeacher(teacher);
        klas.setLocation(location);
        klas.setSchool(school);
        if (s1 != null)
            klas.getStudents().add(s1);
        if (s2 != null)
            klas.getStudents().add(s2);
        if (s3 != null)
            klas.getStudents().add(s3);
        classroomRepository.save(klas);
    }

    private void seedSchoolsAndLocations() {
        if (schoolRepository.count() > 0)
            return;
        School school = schoolRepository.save(
                new School("AP Hogeschool", "", "", "", 10, 14, 14, 3, "aphogeschool"));
        Location location = new Location();
        location.setSchool(school);
        location.setName("Blok A");
        location.setAdres("");
        locationRepository.save(location);
    }
}
