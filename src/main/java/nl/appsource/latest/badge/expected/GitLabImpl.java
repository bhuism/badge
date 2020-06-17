package nl.appsource.latest.badge.expected;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.latest.badge.controller.BadgeException;
import nl.appsource.latest.badge.controller.BadgeStatus;
import nl.appsource.latest.badge.model.gitlab.GitLabResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static nl.appsource.latest.badge.controller.BadgeStatus.Status.ERROR;
import static nl.appsource.latest.badge.controller.BadgeStatus.Status.LATEST;
import static nl.appsource.latest.badge.controller.BadgeStatus.Status.OUTDATED;

@Slf4j
@RequiredArgsConstructor
public class GitLabImpl implements GitLab {

    private static final String ID = "id";
    private static final String REF_NAME = "ref_name";

    private static final String GITLAB_BRANCHES_WHERE_HEAD_URL = "https://gitlab.com/api/v4/projects/{" + ID + "}/repository/commits?ref_name={" + REF_NAME + "}&per_page=1";

    private final RestTemplate restTemplate;

    @Override
    public BadgeStatus getBadgeStatus(final String id, final String branch, final String commit_sha) throws BadgeException {
        return callGitLab(id, branch, commit_sha);
    }

    private BadgeStatus callGitLab(final String id, final String branch, final String commit_sha) throws BadgeException {

        Long duration = null;

        try {

            final HttpHeaders requestHeaders = new HttpHeaders();

            final Map<String, String> vars = new HashMap<>();

            vars.put(ID, id);
            vars.put(REF_NAME, branch);

            final long startTime = System.currentTimeMillis();

            final ResponseEntity<GitLabResponse[]> gitLabResponseEntity = restTemplate.exchange(GITLAB_BRANCHES_WHERE_HEAD_URL, HttpMethod.GET, new HttpEntity<>(requestHeaders), GitLabResponse[].class, vars);


            duration = Math.abs(System.currentTimeMillis() - startTime);

            if (gitLabResponseEntity.getBody() != null && gitLabResponseEntity.getStatusCode().equals(HttpStatus.OK)) {

                final List<GitLabResponse> gitLabResponse = asList(gitLabResponseEntity.getBody());

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
                return new BadgeStatus(ERROR, gitLabResponseEntity.getStatusCode().getReasonPhrase());
            }
        } catch (final Exception e) {
            log.error("Gitlab", e);
            throw new BadgeException(new BadgeStatus(ERROR, "Gitlab:" + e.getLocalizedMessage()));
        } finally {
            log.info("Gitlab: " + id + ", branch=" + branch + ", sha=" + commit_sha + ", duration=" + duration + " msec, ");
        }

    }


}
