package nl.appsource.latest.badge.actual;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.latest.badge.controller.BadgeException;
import nl.appsource.latest.badge.controller.BadgeStatus;
import nl.appsource.latest.badge.model.actuator.Info;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static nl.appsource.latest.badge.controller.BadgeStatus.Status.ERROR;

@Slf4j
@RequiredArgsConstructor
public class Actuator {

    private final RestTemplate restTemplate;

    public String getCommitSha(final String actuator_url) throws BadgeException {

        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        try {

            final ResponseEntity<Info> info = restTemplate.exchange(actuator_url, HttpMethod.GET, new HttpEntity<>(headers), Info.class);

            if (info.getStatusCode().equals(HttpStatus.OK)) {
                return info.getBody().getGit().getCommit().getId();
            } else {
                return info.getStatusCode().getReasonPhrase();
            }

        } catch (Exception e) {
            log.error("actuator: " + actuator_url, e);
            throw new BadgeException(new BadgeStatus(ERROR, "actuator:" + e.getLocalizedMessage()));
        }

    }

}
