package nl.appsource.badge.actual;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.badge.controller.BadgeException;
import nl.appsource.badge.model.actuator.Info;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.function.Function;

import static java.lang.Math.abs;

@Slf4j
@RequiredArgsConstructor
public class Actuator implements Function<String, String> {

    private final RestTemplate restTemplate;

    public String apply(final String actuator_url) throws BadgeException {

        final long startTime = System.currentTimeMillis();

        final HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String result = null;

        try {

            final ResponseEntity<Info> info = restTemplate.exchange(actuator_url, HttpMethod.GET, new HttpEntity<>(headers), Info.class);

            if (info.getStatusCode().equals(HttpStatus.OK)) {
                result = info.getBody().getGit().getCommit().getId();
            } else {
                result = info.getStatusCode().getReasonPhrase();
            }

            return result;

        } catch (Exception e) {
            log.error("actuator: " + actuator_url, e);
            throw new BadgeException("actuator:" + e.getLocalizedMessage());
        } finally {
            log.info("Actuator: " + actuator_url + ", result=" + result + ", duration=" + abs(System.currentTimeMillis() - startTime) + " msec");
        }

    }

}
