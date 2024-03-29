package ch.stefanjucker.refereecoach.security;

import ch.stefanjucker.refereecoach.service.UserService;
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

    private final UserService userService;

    public UserAuthService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = userService.find(username).orElse(null);
        if (user != null) {
            ArrayList<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(user.getRole().name()));
            if (user.isAdmin()) {
                authorities.add(new SimpleGrantedAuthority("ADMIN"));
            }
            return new User(user.getEmail(), user.getPassword(), authorities);
        }

        throw new UsernameNotFoundException("user not found for: " + username);
    }
}
