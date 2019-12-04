package nl.appsource.latest.badge.expected;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.latest.badge.controller.BadgeException;
import nl.appsource.latest.badge.controller.BadgeStatus;
import nl.appsource.latest.badge.controller.BadgeStatus.Status;
import nl.appsource.latest.badge.model.github.GitHubResponse;
import org.springframework.boot.context.properties.ConfigurationProperties;
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

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static nl.appsource.latest.badge.controller.BadgeStatus.Status.ERROR;
import static nl.appsource.latest.badge.controller.BadgeStatus.Status.LATEST;
import static nl.appsource.latest.badge.controller.BadgeStatus.Status.OUTDATED;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Service
@RequiredArgsConstructor
@Slf4j
@ConfigurationProperties(prefix = "badge.github")
public class GitHub {

    private int cacheExpireTimeout = 60;

    private static final String OWNER = "owner";
    private static final String REPO = "repo";
    private static final String COMMIT_SHA = "commit_sha";

    private static final String GIT_BRANCHES_WHERE_HEAD_URL = "https://api.github.com/repos/{" + OWNER + "}/{" + REPO + "}/commits/{" + COMMIT_SHA + "}/branches-where-head";

    private static final MediaType GITHUB_PREVIEW_MEDIATYPE = MediaType.valueOf("application/vnd.github.groot-preview+json");

    private static final String LIMIT = "X-RateLimit-Limit";
    private static final String REMAINING = "X-RateLimit-Remaining";
    private static final String RESET = "X-RateLimit-Reset";

    private Cache<String, Status> cache;

    @PostConstruct
    public void postConstruct() {
        cache = CacheBuilder.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(cacheExpireTimeout, TimeUnit.SECONDS)
                .build()
        ;
    }

    private final RestTemplate restTemplate;

    final BiFunction<HttpHeaders, String, String> safeHeaderPrint = (responseHeaders, key) ->
            key + "=" +
                    Optional.ofNullable(responseHeaders.get(key))
                            .map(Collection::stream)
                            .flatMap(Stream::findFirst)
                            .orElse(null);

    final Function<HttpHeaders, String> safeHeadersPrint = (responseHeaders) ->
            Stream.of(LIMIT, REMAINING, RESET).map(key -> safeHeaderPrint.apply(responseHeaders, key)).collect(joining(", "));

    public BadgeStatus getLatestStatus(final String owner, final String repo, final String branch, final String commit_sha, final String labelText) throws BadgeException {

        final String commit_sha_short = commit_sha.substring(0, min(commit_sha.length(), 7));

        final Status cacheValue = cache.getIfPresent(owner + "/" + repo + "/" + commit_sha);

        if (cacheValue != null) {
            return new BadgeStatus(cacheValue, labelText, commit_sha_short);
        } else {
            return callGutHub(owner, repo, branch, commit_sha, labelText);
        }

    }

    private BadgeStatus callGutHub(final String owner, final String repo, final String branch, final String commit_sha, final String labelText) throws BadgeException {

        try {

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

            final long msec = System.currentTimeMillis();

            final ResponseEntity<GitHubResponse[]> gitHubResponseEntity = restTemplate.exchange(GIT_BRANCHES_WHERE_HEAD_URL, HttpMethod.GET, new HttpEntity<>(headers), GitHubResponse[].class, vars);

            final long duration = Math.abs(System.currentTimeMillis() - msec);

            final HttpHeaders h = gitHubResponseEntity.getHeaders();

            log.info("Github: /repos/" + owner + "/" + repo + "/" + branch + "/" + commit_sha + ", duration=" + duration + " msec, " + safeHeadersPrint.apply(h));

            if (gitHubResponseEntity.getBody() != null && gitHubResponseEntity.getStatusCode().equals(HttpStatus.OK)) {

                final List<GitHubResponse> gitHubResponse = asList(gitHubResponseEntity.getBody());
                final String commit_sha_short = commit_sha.substring(0, min(commit_sha.length(), 7));

                if (gitHubResponse.stream().anyMatch(g -> g.getName().equals(branch))) {
                    cache.put(owner + "/" + repo + "/" + commit_sha, LATEST);
                    return new BadgeStatus(LATEST, labelText, commit_sha_short);
                } else {
                    cache.put(owner + "/" + repo + "/" + commit_sha, OUTDATED);
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
