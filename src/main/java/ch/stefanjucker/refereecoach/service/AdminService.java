package ch.stefanjucker.refereecoach.service;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import ch.stefanjucker.refereecoach.domain.Tags;
import ch.stefanjucker.refereecoach.domain.User;
import ch.stefanjucker.refereecoach.domain.repository.TagsRepository;
import ch.stefanjucker.refereecoach.domain.repository.UserRepository;
import ch.stefanjucker.refereecoach.dto.CreateUserDTO;
import ch.stefanjucker.refereecoach.dto.TagNameDTO;
import ch.stefanjucker.refereecoach.dto.TagUsageDTO;
import ch.stefanjucker.refereecoach.dto.UpdateUserDTO;
import ch.stefanjucker.refereecoach.dto.UserDTO;
import ch.stefanjucker.refereecoach.mapper.DTOMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import java.util.List;

@Slf4j
@Service
public class AdminService {

    private static final DTOMapper DTO_MAPPER = DTOMapper.INSTANCE;

    private final UserRepository userRepository;
    private final TagsRepository tagsRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository, TagsRepository tagsRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tagsRepository = tagsRepository;
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

    public void updateUser(Long id, UpdateUserDTO dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "user not found: " + id));
        // check for email uniqueness if it's changed
        if (!user.getEmail().equals(dto.email()) && userRepository.findByEmail(dto.email()).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "user with email already exists: " + dto.email());
        }

        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setRole(dto.role());
        user.setAdmin(dto.admin());
        userRepository.save(user);
    }

    public List<TagUsageDTO> getAllTags() {
        return tagsRepository.findAllWithUsageCount().stream()
                             .map(tag -> new TagUsageDTO(tag.getId(), tag.getName(), tag.getUsageCount()))
                             .toList();
    }

    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public TagUsageDTO createTag(TagNameDTO dto) {
        String name = dto.name().trim();
        if (tagsRepository.findByNameIgnoreCase(name).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "tag already exists: " + name);
        }

        Tags tag = new Tags();
        tag.setName(name);
        tag = tagsRepository.save(tag);
        return new TagUsageDTO(tag.getId(), tag.getName(), 0);
    }

    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public void renameTag(Long id, TagNameDTO dto) {
        Tags tag = tagsRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "tag not found: " + id));
        String name = dto.name().trim();
        tagsRepository.findByNameIgnoreCase(name)
                      .filter(other -> !other.getId().equals(id))
                      .ifPresent(other -> {
                          throw new ResponseStatusException(CONFLICT, "tag already exists: " + name);
                      });

        tag.setName(name);
        tagsRepository.save(tag);
    }

    @Transactional
    @CacheEvict(value = "tags", allEntries = true)
    public void deleteTag(Long id) {
        Tags tag = tagsRepository.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "tag not found: " + id));
        if (tagsRepository.countUsages(id) > 0) {
            throw new ResponseStatusException(CONFLICT, "tag is still in use: " + tag.getName());
        }

        tagsRepository.delete(tag);
    }
}
