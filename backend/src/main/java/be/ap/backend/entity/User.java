package be.ap.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "username", unique = true, nullable = true, length = 60)
    private String username;

    @Column(name = "password", nullable = true, length = 60)
    private String password;

    @ManyToOne
    @JoinColumn(name = "school_id", nullable = true)
    private School school;

    @Column(name = "ss_id", unique = true, length = 255)
    private String ssId;

    // bij messages terug nodig
    // @Column(name = "ss_refresh", length = 1023)
    // private String ssRefresh;

    @Column(name = "oneroster_id", unique = true, length = 255)
    private String oneRosterId;

    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(role);
    }

}
