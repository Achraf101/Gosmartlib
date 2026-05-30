package be.ap.backend.entity;

import java.io.Serializable;

import be.ap.backend.enums.ComponentScreen;
import be.ap.backend.enums.ComponentType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HiddenComponent implements Serializable {
    private static final long serialVersionUID = 1L;

    @Enumerated(EnumType.STRING)
    @Column(name = "screen")
    private ComponentScreen screen;

    @Enumerated(EnumType.STRING)
    @Column(name = "component_type")
    private ComponentType type;
}
