package be.ap.backend.service;

import be.ap.backend.entity.Challenge;
import be.ap.backend.entity.Loan;
import be.ap.backend.entity.LoanStatus;
import be.ap.backend.entity.UserChallenge;
import be.ap.backend.repository.ChallengeRepository;
import be.ap.backend.repository.LoanRepository;
import be.ap.backend.repository.UserChallengeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class GamificationService {

    private final LoanRepository loanRepository;
    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;

    public int getTotalBooks(Long userId) {
        List<Loan> loans = loanRepository.findByUserId(userId);
        return loans.stream()
                .filter(loan -> loan.getStatus() == LoanStatus.ACCEPTED
                        || loan.getStatus() == LoanStatus.RECEIVED
                        || loan.getStatus() == LoanStatus.RETURNED)
                .mapToInt(loan -> loan.getLoanBooks().size())
                .sum();
    }

    public String getStreakLevel(int totalBooks) {
        if (totalBooks >= 100)
            return "Legendarische lezer";
        if (totalBooks >= 75)
            return "Meestelezer";
        if (totalBooks >= 50)
            return "Bibliofiel";
        if (totalBooks >= 25)
            return "Leesheld";
        if (totalBooks >= 15)
            return "Klassiek";
        if (totalBooks >= 10)
            return "Nachtlezer";
        if (totalBooks >= 5)
            return "5 op rij";
        if (totalBooks >= 3)
            return "Op dreef";
        if (totalBooks >= 1)
            return "Beginner";
        return "Geen level";
    }

    public List<UserChallenge> getChallengesForUser(Long userId) {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        if (!userChallengeRepository.existsByUserIdAndMonth(userId, currentMonth)) {
            assignChallenges(userId, currentMonth);
        }

        return userChallengeRepository.findByUserIdAndMonth(userId, currentMonth);
    }

    private void assignChallenges(Long userId, String month) {
        List<Challenge> all = new ArrayList<>(challengeRepository.findAll());
        Collections.shuffle(all, new java.util.Random(userId + month.hashCode()));
        List<Challenge> selected = all.stream().limit(3).toList();

        for (Challenge challenge : selected) {
            UserChallenge uc = new UserChallenge();
            uc.setUserId(userId);
            uc.setChallenge(challenge);
            uc.setMonth(month);
            uc.setCompleted(false);
            uc.setAssignedAt(LocalDate.now()); // ← datum van toewijzing opslaan
            userChallengeRepository.save(uc);
        }
    }

    public void checkChallenges(Long userId) {
        List<Loan> loans = loanRepository.findByUserId(userId);
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<UserChallenge> challenges = userChallengeRepository.findByUserIdAndMonth(userId, currentMonth);

        for (UserChallenge uc : challenges) {
            if (uc.isCompleted())
                continue;

            Challenge c = uc.getChallenge();
            LocalDate assignedAt = uc.getAssignedAt();

            boolean completed = loans.stream()
                    .filter(loan -> loan.getStatus() == LoanStatus.ACCEPTED
                            || loan.getStatus() == LoanStatus.RECEIVED
                            || loan.getStatus() == LoanStatus.RETURNED)
                    .filter(loan -> assignedAt == null || !loan.getStart().isBefore(assignedAt))
                    .flatMap(loan -> loan.getLoanBooks().stream())
                    .anyMatch(lb -> {
                        return switch (c.getConditionType()) {
                            case "pages" -> lb.getBook().getPages() != null &&
                                    lb.getBook().getPages() >= Integer.parseInt(c.getConditionValue());
                            case "genre" -> lb.getBook().getGenres() != null &&
                                    lb.getBook().getGenres().stream()
                                            .anyMatch(g -> g.getName().equalsIgnoreCase(c.getConditionValue()));
                            case "year" -> lb.getBook().getPublished() != null &&
                                    lb.getBook().getPublished().getValue() <= Integer.parseInt(c.getConditionValue());
                            case "language" -> lb.getBook().getLanguage() != null &&
                                    lb.getBook().getLanguage().getCode().equalsIgnoreCase(c.getConditionValue());
                            default -> false;
                        };
                    });

            if (completed) {
                uc.setCompleted(true);
                uc.setCompletedAt(LocalDate.now());
                userChallengeRepository.save(uc);
            }
        }
    }
}