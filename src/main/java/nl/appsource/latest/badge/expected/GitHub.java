package nl.appsource.latest.badge.expected;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.latest.badge.controller.BadgeException;
import nl.appsource.latest.badge.controller.BadgeStatus;
import nl.appsource.latest.badge.model.github.GitHubResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nl.appsource.latest.badge.controller.BadgeStatus.Status.ERROR;
import static nl.appsource.latest.badge.controller.BadgeStatus.Status.LATEST;
import static nl.appsource.latest.badge.controller.BadgeStatus.Status.OUTDATED;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHub {

    private static final String OWNER = "owner";
    private static final String REPO = "repo";
    private static final String COMMIT_SHA = "commit_sha";

    private static final String GIT_BRANCHES_WHERE_HEAD_URL = "https://api.github.com/repos/{" + OWNER + "}/{" + REPO + "}/commits/{" + COMMIT_SHA + "}/branches-where-head";

    private static final MediaType GITHUB_PREVIEW_MEDIATYPE = MediaType.valueOf("application/vnd.github.groot-preview+json");

    private static final String LIMIT = "X-RateLimit-Limit";
    private static final String REMAINING = "X-RateLimit-Remaining";
    private static final String RESET = "X-RateLimit-Reset";

    private final RestTemplate restTemplate;

    final BiFunction<HttpHeaders, String, String> safeHeaderPrint = (responseHeaders, key) ->
            key + "=" +
                    Optional.ofNullable(responseHeaders.get(key))
                            .map(Collection::stream)
                            .flatMap(Stream::findFirst)
                            .orElse(null);

    final Function<HttpHeaders, String> safeHeadersPrint = (responseHeaders) ->
            Stream.of(LIMIT, REMAINING, RESET).map(key -> safeHeaderPrint.apply(responseHeaders, key)).collect(Collectors.joining(", "));

    public BadgeStatus getLatestStatus(final String owner, final String repo, final String branch, final String commit_sha, final String labelText) throws BadgeException {

        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(GITHUB_PREVIEW_MEDIATYPE));

        final String token = System.getenv("GITHUB_TOKEN");

        if (StringUtils.hasText(token)) {
            headers.add(AUTHORIZATION, "token " + token);
        }

        final Map<String, String> vars = new HashMap<>();

        vars.put(OWNER, owner);
        vars.put(REPO, repo);
        vars.put(COMMIT_SHA, commit_sha);

        try {

            final long msec = System.currentTimeMillis();

            final ResponseEntity<GitHubResponse[]> gitHubResponseEntity = restTemplate.exchange(GIT_BRANCHES_WHERE_HEAD_URL, HttpMethod.GET, new HttpEntity<>(headers), GitHubResponse[].class, vars);

            final long duration = Math.abs(System.currentTimeMillis() - msec);

            final HttpHeaders h = gitHubResponseEntity.getHeaders();

            log.info("Github: /repos/" + owner + "/" + repo + "/" + branch + "/" + commit_sha + ", duration=" + duration + " msec, " + safeHeadersPrint.apply(h));

            if (gitHubResponseEntity.getBody() != null && gitHubResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
                final List<GitHubResponse> gitHubResponse = Arrays.asList(gitHubResponseEntity.getBody());
                final String commit_sha_short = commit_sha.substring(0, Math.min(commit_sha.length(), 7));
                if (gitHubResponse.stream().anyMatch(g -> g.getName().equals(branch))) {
                    return new BadgeStatus(LATEST, labelText, commit_sha_short);
                } else {
                    return new BadgeStatus(OUTDATED, labelText, commit_sha_short);
                }
            } else {
                return new BadgeStatus(ERROR, "github", gitHubResponseEntity.getStatusCode().getReasonPhrase());
            }
        } catch (final HttpClientErrorException.Forbidden f) {
            final HttpHeaders h = f.getResponseHeaders();
            log.warn("Got rate limited by github: " + f.getLocalizedMessage() + ", " + safeHeadersPrint.apply(h));
            throw new BadgeException(new BadgeStatus(ERROR, "github", f.getStatusText()));
        } catch (final HttpClientErrorException f) {
            log.error("Github: " + f.getLocalizedMessage());
            throw new BadgeException(new BadgeStatus(ERROR, "github", f.getStatusText()));
        } catch (final Exception e) {
            log.error("Github", e);
            throw new BadgeException(new BadgeStatus(ERROR, "github", e.getLocalizedMessage()));
        }
    }


}
