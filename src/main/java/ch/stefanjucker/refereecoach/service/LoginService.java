package ch.stefanjucker.refereecoach.service;

import static ch.stefanjucker.refereecoach.util.DateUtil.now;

import ch.stefanjucker.refereecoach.domain.Coach;
import ch.stefanjucker.refereecoach.domain.HasLogin;
import ch.stefanjucker.refereecoach.domain.Referee;
import ch.stefanjucker.refereecoach.domain.repository.CoachRepository;
import ch.stefanjucker.refereecoach.domain.repository.RefereeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class LoginService {

    private final CoachRepository coachRepository;
    private final RefereeRepository refereeRepository;

    public LoginService(CoachRepository coachRepository,
                        RefereeRepository refereeRepository) {
        this.coachRepository = coachRepository;
        this.refereeRepository = refereeRepository;
    }

    public Optional<? extends HasLogin> find(String email) {
        Optional<Coach> coach = coachRepository.findByEmail(email);
        if (coach.isPresent()) {
            return coach;
        }

        return refereeRepository.findByEmail(email);
    }

    public void save(HasLogin user) {
        user.setLastLogin(now());
        if (user instanceof Coach coach) {
            coachRepository.save(coach);
        }

        if (user instanceof Referee referee) {
            refereeRepository.save(referee);
        }
    }
}
