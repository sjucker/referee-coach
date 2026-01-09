package ch.stefanjucker.refereecoach.service;

import ch.stefanjucker.refereecoach.domain.User;
import ch.stefanjucker.refereecoach.domain.repository.UserRepository;
import ch.stefanjucker.refereecoach.dto.UserDTO;
import ch.stefanjucker.refereecoach.mapper.DTOMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AdminService {

    private static final DTOMapper DTO_MAPPER = DTOMapper.INSTANCE;

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
