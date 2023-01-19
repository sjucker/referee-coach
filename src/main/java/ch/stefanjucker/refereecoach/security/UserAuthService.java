package ch.stefanjucker.refereecoach.security;

import ch.stefanjucker.refereecoach.domain.repository.CoachRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserAuthService implements UserDetailsService {

    private final CoachRepository coachRepository;

    public UserAuthService(CoachRepository coachRepository) {
        this.coachRepository = coachRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var coach = coachRepository.findByEmail(username);
        if (coach.isPresent()) {
            ArrayList<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("USER"));
            if (coach.get().isAdmin()) {
                authorities.add(new SimpleGrantedAuthority("ADMIN"));
            }
            return new User(coach.get().getEmail(), coach.get().getPassword(), authorities);
        }

        throw new UsernameNotFoundException("user not found for: " + username);
    }
}
