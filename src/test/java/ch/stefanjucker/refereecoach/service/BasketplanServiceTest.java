package ch.stefanjucker.refereecoach.service;

import static ch.stefanjucker.refereecoach.Fixtures.referee;
import static ch.stefanjucker.refereecoach.dto.OfficiatingMode.OFFICIATING_3PO;
import static ch.stefanjucker.refereecoach.service.BasketplanService.Federation.SBL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ch.stefanjucker.refereecoach.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class BasketplanServiceTest {

    @Mock
    private UserRepository userRepository;

    private BasketplanService basketplanService;

    @BeforeEach
    void setUp() {
        basketplanService = new BasketplanService(userRepository);
    }

    @Test
    void findGameByNumber() {
        // given
        when(userRepository.findByName("Stojcev Bosko")).thenReturn(Optional.of(referee("Stojcev Bosko")));
        when(userRepository.findByName("Balletta Davide")).thenReturn(Optional.of(referee("Balletta Davide")));
        when(userRepository.findByName("Vitalini Fabiano")).thenReturn(Optional.of(referee("Vitalini Fabiano")));

        // when
        var game = basketplanService.findGameByNumber(SBL, "22-00249");

        // then
        assertThat(game).isNotEmpty()
                        .hasValueSatisfying(dto -> {
                            assertThat(dto.gameNumber()).isEqualTo("22-00249");
                            assertThat(dto.date()).isEqualTo("2022-10-01");
                            assertThat(dto.result()).isEqualTo("82 - 98");
                            assertThat(dto.teamA()).isEqualTo("Lugano Tigers");
                            assertThat(dto.teamB()).isEqualTo("Spinelli Massagno");
                            assertThat(dto.officiatingMode()).isEqualTo(OFFICIATING_3PO);
                            assertThat(dto.referee1()).isNotNull();
                            assertThat(dto.referee2()).isNotNull();
                            assertThat(dto.referee3()).isNotNull();
                            assertThat(dto.youtubeId()).isEqualTo("2d9RLdlTihY");
                        });
    }
}
