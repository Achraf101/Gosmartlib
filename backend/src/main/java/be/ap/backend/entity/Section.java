package be.ap.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
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

    public Section(){};
}