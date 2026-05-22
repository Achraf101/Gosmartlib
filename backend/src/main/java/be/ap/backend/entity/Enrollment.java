package be.ap.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "enrollment")
@Data
@NoArgsConstructor
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "one_roster_id", unique = true, length = 255)
    private String oneRosterId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "classroom_id", nullable = true)
    private Classroom classroom;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = true)
    private UserRole role;

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = true)
    private School school;
}