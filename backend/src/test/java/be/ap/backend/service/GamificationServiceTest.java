package be.ap.backend.service;

import be.ap.backend.entity.*;
import be.ap.backend.enums.LoanStatus;
import be.ap.backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GamificationServiceTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private ChallengeRepository challengeRepository;
    @Mock
    private UserChallengeRepository userChallengeRepository;

    @InjectMocks
    private GamificationService gamificationService;

    private final Long USER_ID = 1L;
    private final String CURRENT_MONTH = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

    private Loan buildLoan(LocalDate start, LoanStatus status, Book book) {
        LoanBook loanBook = new LoanBook();
        loanBook.setBook(book);

        Loan loan = new Loan();
        loan.setStart(start);
        loan.setStatus(status);
        loan.setLoanBooks(Set.of(loanBook));
        return loan;
    }

    private UserChallenge buildUserChallenge(Challenge challenge, LocalDate assignedAt) {
        UserChallenge uc = new UserChallenge();
        uc.setChallenge(challenge);
        uc.setAssignedAt(assignedAt);
        uc.setCompleted(false);
        uc.setUserId(USER_ID);
        uc.setMonth(CURRENT_MONTH);
        return uc;
    }

    @Test
    void taalChallenge_Frans_nietVoltooid_leningVoorToewijzing() {
        Challenge challenge = new Challenge();
        challenge.setDescription("Lees een boek in het Frans");
        challenge.setConditionType("language");
        challenge.setConditionValue("fr");

        Language fr = new Language("Frans", "fr");
        Book frenchBook = new Book();
        frenchBook.setLanguage(fr);

        Loan oldLoan = buildLoan(LocalDate.now().minusDays(10), LoanStatus.RETURNED, frenchBook);
        UserChallenge uc = buildUserChallenge(challenge, LocalDate.now());

        when(loanRepository.findByUserId(USER_ID)).thenReturn(List.of(oldLoan));
        when(userChallengeRepository.findByUserIdAndMonth(USER_ID, CURRENT_MONTH)).thenReturn(List.of(uc));

        gamificationService.checkChallenges(USER_ID);

        assertFalse(uc.isCompleted());
        verify(userChallengeRepository, never()).save(any());
    }

    @Test
    void taalChallenge_Frans_voltooid_leningNaToewijzing() {
        Challenge challenge = new Challenge();
        challenge.setDescription("Lees een boek in het Frans");
        challenge.setConditionType("language");
        challenge.setConditionValue("fr");

        Language fr = new Language("Frans", "fr");
        Book frenchBook = new Book();
        frenchBook.setLanguage(fr);

        Loan newLoan = buildLoan(LocalDate.now(), LoanStatus.ACCEPTED, frenchBook);
        UserChallenge uc = buildUserChallenge(challenge, LocalDate.now().minusDays(1));

        when(loanRepository.findByUserId(USER_ID)).thenReturn(List.of(newLoan));
        when(userChallengeRepository.findByUserIdAndMonth(USER_ID, CURRENT_MONTH)).thenReturn(List.of(uc));

        gamificationService.checkChallenges(USER_ID);

        assertTrue(uc.isCompleted());
        verify(userChallengeRepository).save(uc);
    }

    @Test
    void genreChallenge_Fantasy_voltooid_leningNaToewijzing() {
        Challenge challenge = new Challenge();
        challenge.setDescription("Lees een Fantasy boek");
        challenge.setConditionType("genre");
        challenge.setConditionValue("Fantasy");

        Genre fantasy = new Genre("Fantasy");
        Book fantasyBook = new Book();
        fantasyBook.setGenres(Set.of(fantasy));

        Loan newLoan = buildLoan(LocalDate.now(), LoanStatus.RECEIVED, fantasyBook);
        UserChallenge uc = buildUserChallenge(challenge, LocalDate.now().minusDays(1));

        when(loanRepository.findByUserId(USER_ID)).thenReturn(List.of(newLoan));
        when(userChallengeRepository.findByUserIdAndMonth(USER_ID, CURRENT_MONTH)).thenReturn(List.of(uc));

        gamificationService.checkChallenges(USER_ID);

        assertTrue(uc.isCompleted());
    }

    @Test
    void paginaChallenge_500paginas_voltooid_leningNaToewijzing() {
        Challenge challenge = new Challenge();
        challenge.setDescription("Lees een boek van meer dan 500 pagina's");
        challenge.setConditionType("pages");
        challenge.setConditionValue("500");

        Book dik = new Book();
        dik.setPages(546);

        Loan newLoan = buildLoan(LocalDate.now(), LoanStatus.RETURNED, dik);
        UserChallenge uc = buildUserChallenge(challenge, LocalDate.now().minusDays(1));

        when(loanRepository.findByUserId(USER_ID)).thenReturn(List.of(newLoan));
        when(userChallengeRepository.findByUserIdAndMonth(USER_ID, CURRENT_MONTH)).thenReturn(List.of(uc));

        gamificationService.checkChallenges(USER_ID);

        assertTrue(uc.isCompleted());
    }

    @Test
    void jaarChallenge_voor1980_voltooid_leningNaToewijzing() {
        Challenge challenge = new Challenge();
        challenge.setDescription("Lees een boek gepubliceerd voor 1980");
        challenge.setConditionType("year");
        challenge.setConditionValue("1980");

        Book oudBoek = new Book();
        oudBoek.setPublished(Year.of(1962));

        Loan newLoan = buildLoan(LocalDate.now(), LoanStatus.RETURNED, oudBoek);
        UserChallenge uc = buildUserChallenge(challenge, LocalDate.now().minusDays(1));

        when(loanRepository.findByUserId(USER_ID)).thenReturn(List.of(newLoan));
        when(userChallengeRepository.findByUserIdAndMonth(USER_ID, CURRENT_MONTH)).thenReturn(List.of(uc));

        gamificationService.checkChallenges(USER_ID);

        assertTrue(uc.isCompleted());
    }

    @Test
    void challenge_alVoltooid_wordtNietOpniuewVerwerkt() {
        Challenge challenge = new Challenge();
        challenge.setConditionType("language");
        challenge.setConditionValue("fr");

        UserChallenge uc = buildUserChallenge(challenge, LocalDate.now().minusDays(1));
        uc.setCompleted(true); // al voltooid

        when(loanRepository.findByUserId(USER_ID)).thenReturn(List.of());
        when(userChallengeRepository.findByUserIdAndMonth(USER_ID, CURRENT_MONTH)).thenReturn(List.of(uc));

        gamificationService.checkChallenges(USER_ID);

        verify(userChallengeRepository, never()).save(any());
    }

    @Test
    void paginaChallenge_teWeinigPaginas_nietVoltooid() {
        Challenge challenge = new Challenge();
        challenge.setConditionType("pages");
        challenge.setConditionValue("500");

        Book dun = new Book();
        dun.setPages(200);

        Loan loan = buildLoan(LocalDate.now(), LoanStatus.ACCEPTED, dun);
        UserChallenge uc = buildUserChallenge(challenge, LocalDate.now().minusDays(1));

        when(loanRepository.findByUserId(USER_ID)).thenReturn(List.of(loan));
        when(userChallengeRepository.findByUserIdAndMonth(USER_ID, CURRENT_MONTH)).thenReturn(List.of(uc));

        gamificationService.checkChallenges(USER_ID);

        assertFalse(uc.isCompleted());
    }

    @Test
    void challenge_leningNogNietGeaccepteerd_nietVoltooid() {
        Challenge challenge = new Challenge();
        challenge.setConditionType("language");
        challenge.setConditionValue("fr");

        Language fr = new Language("Frans", "fr");
        Book frenchBook = new Book();
        frenchBook.setLanguage(fr);

        Loan loan = buildLoan(LocalDate.now(), LoanStatus.REQUESTED, frenchBook);
        UserChallenge uc = buildUserChallenge(challenge, LocalDate.now().minusDays(1));

        when(loanRepository.findByUserId(USER_ID)).thenReturn(List.of(loan));
        when(userChallengeRepository.findByUserIdAndMonth(USER_ID, CURRENT_MONTH)).thenReturn(List.of(uc));

        gamificationService.checkChallenges(USER_ID);

        assertFalse(uc.isCompleted());
    }
}