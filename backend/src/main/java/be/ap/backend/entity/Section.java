package be.ap.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a configurable section within a school context.
 *
 * <p>
 * Sections are used for organizing or ranking content and can be hidden per
 * school configuration.
 * </p>
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "section")
public class Section {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(length = 255, name = "title")
    private String title;

    @Column(name = "ranking")
    private byte ranking;

    @Column(name = "school_id")
    private Long schoolId;

    @Column(name = "hidden")
    private boolean hidden;
}