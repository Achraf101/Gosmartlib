package be.ap.backend;

import java.time.Year;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import be.ap.backend.entity.TestBook;
import be.ap.backend.entity.Hello;
import be.ap.backend.repository.TestBookRepository;
import be.ap.backend.repository.HelloRepository;
import lombok.RequiredArgsConstructor;

@SpringBootApplication
@RequiredArgsConstructor
public class BackendApplication implements CommandLineRunner {

    private final HelloRepository helloRepository;
    private final TestBookRepository testBookRepository;

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Override
    public void run(String... args) {
        helloRepository.save(new Hello("Database en API werken."));

        if (testBookRepository.count() == 0) {
            testBookRepository.save(createTestBook("9789025866730", "Jip en Jansen",
                    "De avonturen van twee kleuters die samen de wereld ontdekken.", true,
                    Year.of(1953), 160, 5, 834));
            testBookRepository.save(createTestBook("9789021672694", "Pluk van de Petteflet",
                    "Pluk woont in de Petteflet en beleeft avonturen met zijn vrienden.", true,
                    Year.of(1971), 176, 5, 721));
            testBookRepository.save(createTestBook("9789021615875", "Kruistocht in Spijkerbroek",
                    "Dolf reist terug in de tijd naar de Kinderkruistocht van 1212.", true,
                    Year.of(1973), 264, 5, 612));
            testBookRepository.save(createTestBook("9789025876562", "Oorlogsgeheimen",
                    "Een jongen ontdekt geheimen uit de Tweede Wereldoorlog.", true,
                    Year.of(2002), 200, 4, 398));
            testBookRepository.save(createTestBook("9789021618586", "Minoes",
                    "Een kat verandert in een vrouw en helpt een verlegen journalist.", true,
                    Year.of(1970), 176, 5, 567));
            testBookRepository.save(createTestBook("9789021434216", "De Griezelbus",
                    "Onnoval schrijft enge verhalen die echt lijken te worden.", true,
                    Year.of(1988), 144, 4, 489));
            testBookRepository.save(createTestBook("9789025867096", "Otje",
                    "Otje en haar vader Tos beleven avonturen in een restaurant.", true,
                    Year.of(1980), 200, 5, 534));
            testBookRepository.save(createTestBook("9789021680101", "De Brief voor de Koning",
                    "Schildknaap Tiuri moet een geheime brief bezorgen.", true,
                    Year.of(1962), 408, 5, 723));
            testBookRepository.save(createTestBook("9789045115078", "Het Gouden Ei",
                    "Een man zoekt obsessief naar zijn verdwenen vriendin.", true,
                    Year.of(1984), 128, 4, 312));
            testBookRepository.save(createTestBook("9789021669748", "Floddertje",
                    "Het vrolijke verhaal van een ondeugend meisje.", true,
                    Year.of(1968), 80, 4, 445));
            testBookRepository.save(createTestBook("9789025860370", "Pippi Langkous",
                    "Het sterkste meisje ter wereld woont alleen in Villa Kakelbont.", true,
                    Year.of(1945), 160, 5, 890));
            testBookRepository.save(createTestBook("9789021613710", "Abeltje",
                    "Een liftjongen maakt een reis in een vliegende lift.", true,
                    Year.of(1953), 152, 4, 378));
        }
    }

    private TestBook createTestBook(String isbn, String title, String description, boolean fiction, Year published,
            int pages,
            int rating, int ratingCount) {
        TestBook testBook = new TestBook();
        testBook.setIsbn(isbn);
        testBook.setTitle(title);
        testBook.setDescription(description);
        testBook.setFiction(fiction);
        testBook.setPublished(published);
        testBook.setPages(pages);
        testBook.setRating(rating);
        testBook.setRatingCount(ratingCount);
        return testBook;
    }
}
