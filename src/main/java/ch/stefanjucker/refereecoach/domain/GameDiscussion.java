package ch.stefanjucker.refereecoach.domain;

import static java.util.stream.Collectors.toSet;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Entity
@Table(name = "game_discussion")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class GameDiscussion {

    @Id
    @Column(nullable = false)
    private String id;

    @Embedded
    private BasketplanGame basketplanGame;

    public Optional<User> referee1() {
        return Optional.ofNullable(basketplanGame.getReferee1());
    }

    public Optional<User> referee2() {
        return Optional.ofNullable(basketplanGame.getReferee2());
    }

    public Optional<User> referee3() {
        return Optional.ofNullable(basketplanGame.getReferee3());
    }

    public Set<Long> relevantRefereeIds() {
        return Stream.of(referee1().map(User::getId).orElse(0L),
                         referee2().map(User::getId).orElse(0L),
                         referee3().map(User::getId).orElse(0L))
                     .collect(toSet());

    }
}
