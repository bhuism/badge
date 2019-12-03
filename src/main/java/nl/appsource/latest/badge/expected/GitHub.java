package nl.appsource.latest.badge.expected;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.latest.badge.controller.BadgeStatus;
import nl.appsource.latest.badge.model.github.GitHubResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nl.appsource.latest.badge.controller.BadgeStatus.ERROR;
import static nl.appsource.latest.badge.controller.BadgeStatus.LATEST;
import static nl.appsource.latest.badge.controller.BadgeStatus.OUTDATED;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHub {

    private static final String OWNER = "owner";
    private static final String REPO = "repo";
    private static final String COMMIT_SHA = "commit_sha";

    private static final String GIT_BRANCHES_WHERE_HEAD_URL = "https://api.github.com/repos/{" + OWNER + "}/{" + REPO + "}/commits/{" + COMMIT_SHA + "}/branches-where-head";

    private static final MediaType GITHUB_PREVIEW_MEDIATYPE = MediaType.valueOf("application/vnd.github.groot-preview+json");

    private final RestTemplate restTemplate;

    public BadgeStatus getLatestStatus(final String owner, final String repo, final String branch, final String commit_sha) {

        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(GITHUB_PREVIEW_MEDIATYPE));

        final Map<String, String> vars = new HashMap<>();

        vars.put(OWNER, owner);
        vars.put(REPO, repo);
        vars.put(COMMIT_SHA, commit_sha);

        try {
            final ResponseEntity<GitHubResponse[]> gitHubResponseEntity = restTemplate.exchange(GIT_BRANCHES_WHERE_HEAD_URL, HttpMethod.GET, new HttpEntity<>(headers), GitHubResponse[].class, vars);
            if (gitHubResponseEntity.getBody() != null && gitHubResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
                final List<GitHubResponse> gitHubResponse = Arrays.asList(gitHubResponseEntity.getBody());
                if (gitHubResponse.stream().anyMatch(g -> g.getName().equals(branch))) {
                    return LATEST;
                } else {
                    return OUTDATED;
                }
            } else {
                return ERROR;
            }

        } catch (Exception e) {
            log.error("github", e);
            return ERROR;
        }
    }


}
