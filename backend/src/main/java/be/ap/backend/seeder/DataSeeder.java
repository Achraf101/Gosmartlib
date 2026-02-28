package be.ap.backend.seeder;

import be.ap.backend.entity.Book;
import be.ap.backend.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final BookRepository bookRepository;

    public DataSeeder(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) {
        if (bookRepository.count() > 0) return;

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
        bookRepository.save(b4);
    }
}