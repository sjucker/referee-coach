package ch.stefanjucker.refereecoach.service;

import ch.stefanjucker.refereecoach.domain.Coach;
import ch.stefanjucker.refereecoach.domain.repository.CoachRepository;
import ch.stefanjucker.refereecoach.domain.repository.RefereeRepository;
import ch.stefanjucker.refereecoach.dto.RefereeDTO;
import ch.stefanjucker.refereecoach.mapper.DTOMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AdminService {

    private static final DTOMapper DTO_MAPPER = DTOMapper.INSTANCE;

    private final RefereeRepository refereeRepository;
    private final CoachRepository coachRepository;

    public AdminService(RefereeRepository refereeRepository, CoachRepository coachRepository) {
        this.refereeRepository = refereeRepository;
        this.coachRepository = coachRepository;
    }

    public boolean isAdmin(String username) {
        return coachRepository.findByEmail(username)
                              .map(Coach::isAdmin)
                              .orElse(false);
    }

    public List<RefereeDTO> getAllReferess() {
        return refereeRepository.findAll().stream()
                                .map(DTO_MAPPER::toDTO)
                                .toList();
    }
}
