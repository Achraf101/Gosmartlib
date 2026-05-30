package be.ap.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Tracks a user's progress on a monthly challenge assignment.
 *
 * <p>
 * Stores assignment metadata, completion state, and enforces uniqueness per
 * user, month, and challenge.
 * </p>
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "user_challenge", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "month", "challenge_id" })
})
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