package ch.stefanjucker.refereecoach.service;

import static ch.stefanjucker.refereecoach.dto.OfficiatingMode.OFFICIATING_2PO;
import static ch.stefanjucker.refereecoach.dto.OfficiatingMode.OFFICIATING_3PO;
import static ch.stefanjucker.refereecoach.service.BasketplanService.Federation.SBL;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;

import ch.stefanjucker.refereecoach.domain.User;
import ch.stefanjucker.refereecoach.domain.repository.UserRepository;
import ch.stefanjucker.refereecoach.dto.BasketplanGameDTO;
import ch.stefanjucker.refereecoach.dto.UserDTO;
import ch.stefanjucker.refereecoach.mapper.DTOMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class BasketplanService {

    private static final DTOMapper DTO_MAPPER = DTOMapper.INSTANCE;

    private static final Pattern YOUTUBE_ID_PATTERN = Pattern.compile("v=([^&]+)");
    private static final String SEARCH_GAMES_URL = "https://www.basketplan.ch/showSearchGames.do?actionType=searchGames&gameNumber=%s&xmlView=true&perspective=de_default&federationId=%d";

    private final UserRepository userRepository;

    public BasketplanService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<BasketplanGameDTO> findGameByNumber(String gameNumber) {
        return findGameByNumber(SBL, gameNumber);
    }

    public Optional<BasketplanGameDTO> findGameByNumber(Federation federation, String gameNumber) {
        // TODO validate arguments

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            dbf.setFeature(FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(SEARCH_GAMES_URL.formatted(gameNumber, federation.getId()));
            doc.getDocumentElement().normalize();

            NodeList games = doc.getDocumentElement().getElementsByTagName("game");
            if (games.getLength() == 1) {
                var gameNode = games.item(0);

                var leagueHoldingNode = ((Element) gameNode).getElementsByTagName("leagueHolding").item(0);
                var homeTeamNode = ((Element) gameNode).getElementsByTagName("homeTeam").item(0);
                var guestTeamNode = ((Element) gameNode).getElementsByTagName("guestTeam").item(0);
                var resultNode = ((Element) gameNode).getElementsByTagName("result").item(0);

                return Optional.of(new BasketplanGameDTO(
                        gameNumber,
                        getAttributeValue(leagueHoldingNode, "name").orElse("?"),
                        LocalDate.parse(getAttributeValue(gameNode, "date").orElseThrow()),
                        "%s - %s".formatted(getAttributeValue(resultNode, "homeTeamScore").orElse("?"),
                                            getAttributeValue(resultNode, "guestTeamScore").orElse("?")),
                        getAttributeValue(homeTeamNode, "name").orElseThrow(),
                        getAttributeValue(guestTeamNode, "name").orElseThrow(),
                        getAttributeValue(gameNode, "referee3Name").isPresent() ? OFFICIATING_3PO : OFFICIATING_2PO,
                        getReferee(gameNode, "referee1Name"),
                        getReferee(gameNode, "referee2Name"),
                        getReferee(gameNode, "referee3Name"),
                        getAttributeValue(gameNode, "videoLink").map(this::getVideoId).orElse(null)
                ));
            }

        } catch (Exception e) {
            log.error("unexpected error while retrieving game-info from Basketplan", e);
        }

        return Optional.empty();
    }

    private UserDTO getReferee(Node gameNode, String name) {
        Optional<String> refereeName = getAttributeValue(gameNode, name);
        if (refereeName.isPresent()) {
            Optional<User> referee = userRepository.findByName(StringUtils.strip(refereeName.get()));
            if (referee.isPresent()) {
                return DTO_MAPPER.toDTO(referee.get());
            } else {
                log.error("referee '{}' not found in database", refereeName.get());
            }
        }
        return null;
    }

    private String getVideoId(String youtubeLink) {
        Matcher matcher = YOUTUBE_ID_PATTERN.matcher(youtubeLink);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Optional<String> getAttributeValue(Node parentNode, String name) {
        var node = parentNode.getAttributes().getNamedItem(name);
        return node != null ? Optional.ofNullable(node.getNodeValue()) : Optional.empty();
    }

    public enum Federation {
        SBL(12),
        PROBASKET(10);

        private final int id;

        Federation(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

}
