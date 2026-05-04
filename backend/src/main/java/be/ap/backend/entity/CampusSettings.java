package be.ap.backend.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import be.ap.backend.enums.ComponentScreen;
import be.ap.backend.enums.ComponentType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "campus_settings")
public class CampusSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "campus_id")
    private Long campusId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campus_id")
    private Campus campus;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "campus_hidden_components", joinColumns = @JoinColumn(name = "campus_id"))
    private Set<HiddenComponent> hiddenComponents = new HashSet<>();

    public boolean isVisible(ComponentScreen screen, ComponentType type) {
        return hiddenComponents.stream()
            .noneMatch(c -> c.getScreen() == screen && c.getType() == type);
    }
}
