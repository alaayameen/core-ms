package com.social.core.utils;

import com.social.core.gateway.ServiceLocationResolver;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Component
@Log4j2
@AllArgsConstructor
public class commonUtils {

    private final static long OFFSET = 1000000;
    private final static String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private ServiceLocationResolver serviceLocationResolver;

    public URL buildUrl(String serviceName, String basePath) {
        String textApiEndpoint = serviceLocationResolver.resolve(serviceName);

        try {
            URL url = new URL(basePath);
            URL envUrl = new URL(textApiEndpoint);
            URL newUrl = new URL(envUrl.getProtocol(), envUrl.getHost(), envUrl.getPort(), url.getFile());
            return newUrl;
        } catch (MalformedURLException e) {
            log.error(e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"FAILED_DURING_BUILDING_BASEPATH");
        }
    }

    /**
     *
     * @param localDateTime
     * @return Date with pattern "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
     *
     *         The conversion trims last zero(s) from milliSeconds, so the following
     *         workaround is to make sure the milliSeconds last digit is not zero.
     *         Examples: "2022-12-19T03:19:35.098Z" => ""2022-12-19T03:19:35.099Z
     *         "2022-12-19T03:19:35.099Z" => ""2022-12-19T03:19:35.101Z
     */
    public OffsetDateTime convertLocalDateTimeToFormattedOffsetDateTime(LocalDateTime localDateTime) {
        long milliSecondsToAdd = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(localDateTime.format(formatter));
        if ((offsetDateTime.getNano() / OFFSET) % 10 == 9) {
            milliSecondsToAdd = 2;
        }
        return offsetDateTime.plusNanos(milliSecondsToAdd * OFFSET).truncatedTo(ChronoUnit.MILLIS);
    }
}
