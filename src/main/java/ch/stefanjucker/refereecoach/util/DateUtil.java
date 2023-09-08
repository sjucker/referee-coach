package ch.stefanjucker.refereecoach.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

public final class DateUtil {
    private DateUtil() {
    }

    public static LocalDateTime now() {
        return LocalDateTime.now(ZoneId.of("Europe/Zurich"));
    }

}
