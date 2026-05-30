package be.ap.backend.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Represents a classroom within a school context.
 *
 * <p>
 * A classroom can have a teacher, associated school, enrolled users, and
 * metadata such as visibility.
 * </p>
 */
@Entity
@Table(name = "classroom")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", nullable = true, length = 255)
    private String name;

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = true)
    private School school;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = true)
    private User teacher;

    @Column(name = "hidden", nullable = false)
    private boolean hidden = false;

    @Column(name = "ss_id", unique = true, length = 255)
    private String ssId;

    @JsonIgnore
    @OneToMany(mappedBy = "classroom")
    private Set<Enrollment> enrollments = new HashSet<>();

    public Set<User> getStudents() {
        return enrollments.stream()
                .filter(e -> e.getRole() == UserRole.STUDENT)
                .map(Enrollment::getUser)
                .collect(java.util.stream.Collectors.toSet());
    }
}
