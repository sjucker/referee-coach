package ch.stefanjucker.refereecoach.domain;

import static ch.stefanjucker.refereecoach.dto.UserRole.REFEREE;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

import ch.stefanjucker.refereecoach.dto.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Referee implements HasNameEmail, HasLogin {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Enumerated(STRING)
    private RefereeLevel level;

    @Column(nullable = false)
    @ToString.Exclude
    private String password;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Override
    public boolean isAdmin() {
        return false;
    }

    @Override
    public UserRole getRole() {
        return REFEREE;
    }

    public enum RefereeLevel {
        GROUP_1,
        GROUP_2,
        GROUP_3
    }
}
