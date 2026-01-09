package ch.stefanjucker.refereecoach.service;

import static org.springframework.http.HttpStatus.CONFLICT;

import ch.stefanjucker.refereecoach.domain.User;
import ch.stefanjucker.refereecoach.domain.repository.UserRepository;
import ch.stefanjucker.refereecoach.dto.CreateUserDTO;
import ch.stefanjucker.refereecoach.dto.UserDTO;
import ch.stefanjucker.refereecoach.mapper.DTOMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
public class AdminService {

    private static final DTOMapper DTO_MAPPER = DTOMapper.INSTANCE;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isAdmin(String username) {
        return userRepository.findByEmail(username)
                             .map(User::isAdmin)
                             .orElse(false);
    }

    public List<UserDTO> getAllReferees() {
        return userRepository.findAll().stream()
                             .filter(User::isReferee)
                             .map(DTO_MAPPER::toDTO)
                             .toList();
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                             .map(DTO_MAPPER::toDTO)
                             .toList();
    }

    public void createUser(CreateUserDTO dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "user with email already exists: " + dto.email());
        }

        User user = new User();
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setRole(dto.role());
        user.setAdmin(dto.admin());
        user.setPassword(passwordEncoder.encode(dto.password()));
        userRepository.save(user);
    }
}
