package ch.stefanjucker.refereecoach.domain;

import static ch.stefanjucker.refereecoach.dto.UserRole.COACH;
import static jakarta.persistence.GenerationType.IDENTITY;

import ch.stefanjucker.refereecoach.dto.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Coach implements HasNameEmail, HasLogin {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @ToString.Exclude
    private String password;

    @Column(nullable = false)
    private boolean admin;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Override
    public UserRole getRole() {
        return COACH;
    }
}
