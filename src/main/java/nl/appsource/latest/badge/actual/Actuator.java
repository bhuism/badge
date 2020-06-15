package nl.appsource.latest.badge.actual;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.latest.badge.controller.BadgeException;
import nl.appsource.latest.badge.controller.BadgeStatus;
import nl.appsource.latest.badge.model.actuator.Info;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

import static nl.appsource.latest.badge.controller.BadgeStatus.Status.ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class Actuator {

    public String getCommitSha(final String actuator_url) throws BadgeException {

        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        try {

            final Info info = WebClient.builder()
                    .baseUrl(actuator_url)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build()
                    .get()
                    .retrieve()
                    .bodyToMono(Info.class)
                    .block();

            return info.getGit().getCommit().getId();

        } catch (Exception e) {
            log.error("actuator: " + actuator_url, e);
            throw new BadgeException(new BadgeStatus(ERROR, "actuator:" + e.getLocalizedMessage()));
        }

    }

}
