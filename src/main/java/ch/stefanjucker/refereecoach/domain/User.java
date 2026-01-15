package ch.stefanjucker.refereecoach.domain;

import static ch.stefanjucker.refereecoach.dto.UserRole.COACH;
import static ch.stefanjucker.refereecoach.dto.UserRole.REFEREE;
import static ch.stefanjucker.refereecoach.dto.UserRole.REFEREE_COACH;
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
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "login")
@Getter
@Setter
@ToString(of = {"name", "admin", "role"})
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "password_reset_token")
    private String passwordResetToken;

    @Column(nullable = false)
    private boolean admin;

    @Column(nullable = false)
    @Enumerated(STRING)
    private UserRole role;

    public boolean isCoach() {
        return role == COACH;
    }

    public boolean isRefereeCoach() {
        return role == REFEREE_COACH;
    }

    public boolean isReferee() {
        return role == REFEREE || role == REFEREE_COACH;
    }

}
