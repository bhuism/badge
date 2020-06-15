package nl.appsource.latest.badge.expected;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.latest.badge.controller.BadgeException;
import nl.appsource.latest.badge.controller.BadgeStatus;
import nl.appsource.latest.badge.model.github.GitHubResponse;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collection;
import java.util.List;
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
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;

@Service
@RequiredArgsConstructor
@Slf4j
@ConfigurationProperties(prefix = "badge.github")
public class GitHubImpl implements GitHub {

    @Getter
    @Setter
    private int cacheExpireTimeoutSeconds;

    private static final String OWNER = "owner";
    private static final String REPO = "repo";
    private static final String COMMIT_SHA = "commit_sha";

    private static final String GIT_BRANCHES_WHERE_HEAD_URL = "https://api.github.com/repos/{" + OWNER + "}/{" + REPO + "}/commits/{" + COMMIT_SHA + "}/branches-where-head";

    private static final String GITHUB_PREVIEW_MEDIATYPE_VALUE = "application/vnd.github.groot-preview+json";

    private static final String LIMIT = "X-RateLimit-Limit";
    private static final String REMAINING = "X-RateLimit-Remaining";
    private static final String RESET = "X-RateLimit-Reset";

    private Cache<String, BadgeStatus> cache;

    public synchronized Cache<String, BadgeStatus> getCache() {

        if (cache == null) {

            cache = CacheBuilder.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(cacheExpireTimeoutSeconds, TimeUnit.SECONDS)
                    .build();

        }

        return cache;

    }

    final BiFunction<HttpHeaders, String, String> safeHeaderPrint = (responseHeaders, key) ->
            responseHeaders == null ? null :
                    key + "=" + Optional.ofNullable(responseHeaders.get(key))
                            .map(Collection::stream)
                            .flatMap(Stream::findFirst)
                            .orElse(null);

    final Function<HttpHeaders, String> safeHeadersPrint = (responseHeaders) ->
            Stream.of(LIMIT, REMAINING, RESET).map(key -> safeHeaderPrint.apply(responseHeaders, key)).collect(joining(", "));

    public BadgeStatus getBadgeStatus(final String owner, final String repo, final String branch, final String commit_sha) throws BadgeException {

        final BadgeStatus cacheValue = getCache().getIfPresent(owner + "/" + repo + "/" + commit_sha);

        if (cacheValue != null) {
            return cacheValue;
        } else {
            return callGitHub(owner, repo, branch, commit_sha);
        }

    }

    private BadgeStatus callGitHub(final String owner, final String repo, final String branch, final String commit_sha) throws BadgeException {

        Long duration = null;

        HttpHeaders responseHeaders = null;

        try {

            final String token = System.getenv("GITHUB_TOKEN");

            if (StringUtils.isEmpty(token)) {
                log.warn("Empty GITHUB_TOKEN");
            }

            final long startTime = System.currentTimeMillis();

            final ClientResponse clientResponse = WebClient
                    .builder()
                    .baseUrl("https://api.github.com/repos")
                    .defaultHeader(AUTHORIZATION, "token " + token)
                    .defaultHeader(ACCEPT, GITHUB_PREVIEW_MEDIATYPE_VALUE)
                    .build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/{" + OWNER + "}/{" + REPO + "}/commits/{" + COMMIT_SHA + "}/branches-where-head")
                            .build(owner, repo, commit_sha))
                    .exchange()
                    .block();

            duration = Math.abs(System.currentTimeMillis() - startTime);

            if (clientResponse.statusCode().equals(OK)) {

                final List<GitHubResponse> gitHubResponse = asList(clientResponse.bodyToMono(GitHubResponse[].class).block());
                final String commit_sha_short = commit_sha.substring(0, min(commit_sha.length(), 7));
                final BadgeStatus badgeStatus;

                if (gitHubResponse.stream().anyMatch(g -> g.getName().equals(branch))) {
                    badgeStatus = new BadgeStatus(LATEST, commit_sha_short);
                } else {
                    badgeStatus = new BadgeStatus(OUTDATED, commit_sha_short);
                }

                getCache().put(owner + "/" + repo + "/" + commit_sha, badgeStatus);
                return badgeStatus;

            } else {
                return new BadgeStatus(ERROR, clientResponse.statusCode().getReasonPhrase());
            }
        } catch (final HttpClientErrorException.Forbidden f) {
            final HttpHeaders h = f.getResponseHeaders();
            log.warn("Got rate limited by github: " + f.getLocalizedMessage() + ", " + safeHeadersPrint.apply(h));
            throw new BadgeException(new BadgeStatus(ERROR, "Github:" + f.getStatusText()));
        } catch (final HttpClientErrorException f) {
            log.error("Github: " + f.getLocalizedMessage());
            throw new BadgeException(new BadgeStatus(ERROR, "Github:" + f.getStatusText()));
        } catch (final Exception e) {
            log.error("Github", e);
            throw new BadgeException(new BadgeStatus(ERROR, "Github:" + e.getLocalizedMessage()));
        } finally {
            log.info("Github: " + owner + "/" + repo + ", branch=" + branch + ", sha=" + commit_sha + ", duration=" + duration + " msec, " + safeHeadersPrint.apply(responseHeaders));
        }

    }

}
