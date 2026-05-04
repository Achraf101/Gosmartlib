package be.ap.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "user_challenge")
public class UserChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(name = "month", nullable = false)
    private String month;

    @Column(name = "completed")
    private boolean completed = false;

    @Column(name = "completed_at")
    private LocalDate completedAt;

    @Column(name = "assigned_at")
    private LocalDate assignedAt;

}