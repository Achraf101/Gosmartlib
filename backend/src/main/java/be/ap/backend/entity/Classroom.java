package be.ap.backend.entity;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "classroom")
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = { "students" })
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = true)
    private School school;

    @ManyToOne
    @JoinColumn(name = "campus_id", nullable = true)
    private Campus campus;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(name = "hidden", nullable = false)
    private boolean hidden = false;

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "classroom_student",
            joinColumns = @JoinColumn(name = "classroom_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> students = new HashSet<>();
}
