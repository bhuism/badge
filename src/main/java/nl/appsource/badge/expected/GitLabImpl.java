package nl.appsource.badge.expected;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.badge.BadgeException;
import nl.appsource.badge.model.gitlab.GitLabResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.abs;
import static java.util.Arrays.asList;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
public class GitLabImpl implements GitLab {

    private static final String ID = "id";
    private static final String REF_NAME = "ref_name";

    private final RestTemplate restTemplate;

    @Override
    public String apply(final GitLabKey key) throws BadgeException {

        final String id = key.getId();
        final String branch = key.getBranch();

        final long startTime = System.currentTimeMillis();

        try {

            final HttpHeaders requestHeaders = new HttpHeaders();

            final String token = getToken();

            if (StringUtils.hasText(token)) {
                requestHeaders.add(AUTHORIZATION, "bearer " + token);
            }

            final Map<String, String> vars = new HashMap<>();

            vars.put(ID, id);
            vars.put(REF_NAME, branch);

            final ResponseEntity<GitLabResponse[]> gitLabResponseEntity = restTemplate.exchange(getBranchesWhereHeadUrl(), HttpMethod.GET, new HttpEntity<>(requestHeaders), GitLabResponse[].class, vars);

            if (gitLabResponseEntity.getBody() != null && gitLabResponseEntity.getStatusCode().equals(HttpStatus.OK)) {

                final List<GitLabResponse> gitLabResponse = asList(gitLabResponseEntity.getBody());

                return gitLabResponse
                    .stream()
                    .findFirst()
                    .map(GitLabResponse::getShort_id)
                    .get();

            } else {
                throw new BadgeException(gitLabResponseEntity.getStatusCode().getReasonPhrase());
            }
        } catch (HttpClientErrorException e) {
            log.error("gitlab: " + e.getLocalizedMessage());
            throw new BadgeException("actuator:" + e.getStatusText());
        } catch (final Exception e) {
            log.error("Gitlab", e);
            throw new BadgeException("gitlab:" + e.getLocalizedMessage());
        } finally {
            log.info("Gitlab: " + id + ", branch=" + branch + ", duration=" + abs(System.currentTimeMillis() - startTime) + " msec, ");
        }

    }

    private String getBranchesWhereHeadUrl() {
        return getUrl() + "/projects/{" + ID + "}/repository/commits?ref_name={" + REF_NAME + "}&per_page=1";
    }

    private String getUrl() {

        final String url = System.getenv("GITLAB_URL");

        if (StringUtils.isEmpty(url)) {
            return "https://gitlab.com/api/v4";
        } else {
            return url;
        }

    }

    private String getToken() {

        final String token = System.getenv("GITLAB_TOKEN");

        if (StringUtils.isEmpty(token)) {
            log.error("Empty GITLAB_TOKEN");
        }

        return token;

    }


}
