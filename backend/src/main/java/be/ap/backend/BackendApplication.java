package be.ap.backend;

import java.time.Year;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import be.ap.backend.entity.Book;
import be.ap.backend.entity.Hello;
import be.ap.backend.repository.BookRepository;
import be.ap.backend.repository.HelloRepository;
import lombok.RequiredArgsConstructor;

@SpringBootApplication
@RequiredArgsConstructor
public class BackendApplication implements CommandLineRunner {

    private final HelloRepository helloRepository;
    private final BookRepository bookRepository;

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Override
    public void run(String... args) {
        helloRepository.save(new Hello("Database en API werken."));

        if (bookRepository.count() == 0) {
            bookRepository.save(createBook("9780061120084", "To Kill a Mockingbird",
                    "Een klassieker over rechtvaardigheid in het zuiden van Amerika.", true, Year.of(1960), 281, 4,
                    312));
            bookRepository.save(createBook("9780452284234", "1984",
                    "Dystopische roman over totalitarisme en surveillance.", true, Year.of(1949), 328, 5, 587));
            bookRepository.save(createBook("9780743273565", "The Great Gatsby",
                    "Het verhaal van de mysterieuze miljonair Jay Gatsby.", true, Year.of(1925), 180, 4, 245));
            bookRepository.save(createBook("9780316769480", "The Catcher in the Rye",
                    "Een tiener dwaalt door New York na zijn zoveelste schorsing.", true, Year.of(1951), 234, 3, 198));
            bookRepository.save(createBook("9780134685991", "The Pragmatic Programmer",
                    "Praktische gids voor softwareontwikkeling.", false, Year.of(1999), 352, 5, 421));
            bookRepository.save(createBook("9780201633610", "Design Patterns",
                    "Gang of Four patronen voor objectgeoriënteerd ontwerp.", false, Year.of(1994), 395, 4, 356));
            bookRepository.save(createBook("9780140283297", "The Hobbit",
                    "Bilbo Baggins gaat op avontuur met dwergen en een tovenaar.", true, Year.of(1937), 310, 5, 670));
            bookRepository.save(createBook("9780060935467", "Go Set a Watchman", "Go Set a Watchman — het vervolg.",
                    true, Year.of(2015), 278, 3, 89));
            bookRepository.save(createBook("9780596517748", "JavaScript: The Good Parts",
                    "De beste onderdelen van JavaScript uitgelegd.", false, Year.of(2008), 176, 4, 203));
            bookRepository.save(createBook("9780321125217", "Domain-Driven Design",
                    "Complexe software tackelen met domeinmodellering.", false, Year.of(2003), 560, 4, 178));
            bookRepository.save(createBook("9780140449136", "Crime and Punishment",
                    "Een student pleegt een moord en worstelt met schuld.", true, Year.of(1866), 671, 5, 445));
            bookRepository.save(createBook("9782070360248", "Le Petit Prince",
                    "Een poëtisch verhaal over een kleine prins van een andere planeet.", true, Year.of(1943), 96, 5,
                    812));
        }
    }

    private Book createBook(String isbn, String title, String description, boolean fiction, Year published, int pages,
            int rating, int ratingCount) {
        Book book = new Book();
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setDescription(description);
        book.setFiction(fiction);
        book.setPublished(published);
        book.setPages(pages);
        book.setRating(rating);
        book.setRatingCount(ratingCount);
        return book;
    }
}
