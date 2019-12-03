package nl.appsource.latest.badge.actual;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.latest.badge.model.actuator.Info;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class Actuator {

    private final RestTemplate restTemplate;

    public String getCommitSha(final String actuator_url) {

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
            return null;
        }

    }

}
