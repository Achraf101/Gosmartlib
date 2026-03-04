package be.ap.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Hello {
    @Id
    @GeneratedValue
    private Long id;

    private String msg;

    public Hello(String msg) {
        this.msg = msg;
    }
}
