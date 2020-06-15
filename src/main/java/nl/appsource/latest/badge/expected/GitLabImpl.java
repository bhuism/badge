package nl.appsource.latest.badge.expected;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.latest.badge.controller.BadgeException;
import nl.appsource.latest.badge.controller.BadgeStatus;
import nl.appsource.latest.badge.model.gitlab.GitLabResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static nl.appsource.latest.badge.controller.BadgeStatus.Status.ERROR;
import static nl.appsource.latest.badge.controller.BadgeStatus.Status.LATEST;
import static nl.appsource.latest.badge.controller.BadgeStatus.Status.OUTDATED;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpStatus.OK;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitLabImpl implements GitLab {

    private static final String ID = "id";
    private static final String REF_NAME = "ref_name";

    private static final String GITLAB_BRANCHES_WHERE_HEAD_URL = "https://gitlab.com/api/v4/projects/{" + ID + "}/repository/commits?ref_name={" + REF_NAME + "}&per_page=1";

    @Override
    public BadgeStatus getBadgeStatus(final String id, final String branch, final String commit_sha) throws BadgeException {
        return callGitLab(id, branch, commit_sha);
    }

    private BadgeStatus callGitLab(final String id, final String branch, final String commit_sha) throws BadgeException {

        Long duration = null;

        try {

            final long startTime = System.currentTimeMillis();

            final ClientResponse clientResponse = WebClient
                    .builder()
                    .baseUrl("https://gitlab.com/api/v4/projects")
                    .defaultHeader(ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/{" + ID + "}/repository/commits")
                            .queryParam("ref_name", branch)
                            .queryParam("per_page", 1)
                            .build(id))
                    .exchange()
                    .block();

            duration = Math.abs(System.currentTimeMillis() - startTime);

            if (clientResponse.statusCode().equals(OK)) {

                final List<GitLabResponse> gitLabResponse = asList(clientResponse.bodyToMono(GitLabResponse[].class).block());

                final String commit_sha_short = commit_sha.substring(0, min(commit_sha.length(), 8));

                final BadgeStatus badgeStatus;

                if (gitLabResponse
                        .stream()
                        .findFirst()
                        .filter(commit -> commit_sha_short.equals(commit.getShort_id()))
                        .isPresent()) {
                    badgeStatus = new BadgeStatus(LATEST, commit_sha_short);
                } else {
                    badgeStatus = new BadgeStatus(OUTDATED, commit_sha_short);
                }

                return badgeStatus;

            } else {
                return new BadgeStatus(ERROR, clientResponse.statusCode().getReasonPhrase());
            }
        } catch (final Exception e) {
            log.error("Gitlab", e);
            throw new BadgeException(new BadgeStatus(ERROR, "Gitlab:" + e.getLocalizedMessage()));
        } finally {
            log.info("Gitlab: " + id + ", branch=" + branch + ", sha=" + commit_sha + ", duration=" + duration + " msec, ");
        }

    }


}
