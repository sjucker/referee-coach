package ch.stefanjucker.refereecoach.domain;

import static ch.stefanjucker.refereecoach.dto.UserRole.COACH;
import static ch.stefanjucker.refereecoach.dto.UserRole.REFEREE;

import ch.stefanjucker.refereecoach.dto.UserRole;

import java.time.LocalDateTime;

public interface HasLogin {

    Long getId();

    String getEmail();

    String getName();

    String getPassword();

    void setPassword(String password);

    void setLastLogin(LocalDateTime lastLogin);

    boolean isAdmin();

    UserRole getRole();

    default boolean isCoach() {
        return getRole() == COACH;
    }

    default boolean isReferee() {
        return getRole() == REFEREE;
    }

}
