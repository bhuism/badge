package nl.appsource.badge.expected;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.badge.controller.BadgeException;
import nl.appsource.badge.controller.BadgeStatus;
import nl.appsource.badge.model.github.GitHubResponse;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.util.stream.Collectors.joining;
import static nl.appsource.badge.BadgeApplication.cache;
import static nl.appsource.badge.controller.BadgeStatus.Status.ERROR;
import static nl.appsource.badge.controller.BadgeStatus.Status.LATEST;
import static nl.appsource.badge.controller.BadgeStatus.Status.OUTDATED;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubImpl implements GitHub {

    private static final String OWNER = "owner";
    private static final String REPO = "repo";
    private static final String COMMIT_SHA = "commit_sha";
    private static final String GIT_BRANCHES_WHERE_HEAD_URL = "https://api.github.com/repos/{" + OWNER + "}/{" + REPO + "}/commits/{" + COMMIT_SHA + "}/branches-where-head";
    private static final MediaType GITHUB_PREVIEW_MEDIATYPE = MediaType.valueOf("application/vnd.github.groot-preview+json");

    private static final String LIMIT = "X-RateLimit-Limit";
    private static final String REMAINING = "X-RateLimit-Remaining";
    private static final String RESET = "X-RateLimit-Reset";

    private final RestTemplate restTemplate;

    private final Environment environment;

    private final BiFunction<HttpHeaders, String, String> safeHeaderPrint = (responseHeaders, key) ->
        responseHeaders == null ? null :
            key + "=" + Optional.ofNullable(responseHeaders.get(key))
                .map(Collection::stream)
                .flatMap(Stream::findFirst)
                .orElse(null);

    private final Function<HttpHeaders, String> safeHeadersPrint = (responseHeaders) ->
        Stream.of(LIMIT, REMAINING, RESET).map(key -> safeHeaderPrint.apply(responseHeaders, key)).collect(joining(", "));


    public BadgeStatus getBadgeStatus(final String owner, final String repo, final String branch, final String commit_sha) throws BadgeException {

        return cache.computeIfAbsent(getKey(owner, repo, branch, commit_sha), (a) -> callGitHub(owner, repo, branch, commit_sha));

//        if (cacheValue != null) {
//            log.info("Cache hit");
//            return cacheValue;
//        } else {
//            log.info("Cache miss");
//            return ;
//        }

    }

    private BadgeStatus callGitHub(final String owner, final String repo, final String branch, final String commit_sha) {

        final long startTime = System.currentTimeMillis();

        HttpHeaders responseHeaders = null;

        try {

            final HttpHeaders requestHeaders = new HttpHeaders();

            requestHeaders.setAccept(Collections.singletonList(GITHUB_PREVIEW_MEDIATYPE));

            final String token = getToken();

            if (StringUtils.hasText(token)) {
                requestHeaders.add(AUTHORIZATION, "token " + token);
            }

            final Map<String, String> vars = new HashMap<>();

            vars.put(OWNER, owner);
            vars.put(REPO, repo);
            vars.put(COMMIT_SHA, commit_sha);


            final ResponseEntity<GitHubResponse[]> gitHubResponseEntity = restTemplate.exchange(GIT_BRANCHES_WHERE_HEAD_URL, HttpMethod.GET, new HttpEntity<>(requestHeaders), GitHubResponse[].class, vars);

            responseHeaders = gitHubResponseEntity.getHeaders();

            if (gitHubResponseEntity.getBody() != null && gitHubResponseEntity.getStatusCode().equals(HttpStatus.OK)) {

                final List<GitHubResponse> gitHubResponse = Arrays.asList(gitHubResponseEntity.getBody());

                final String commit_sha_short = commit_sha.substring(0, min(commit_sha.length(), 7));
                final BadgeStatus badgeStatus;

                if (gitHubResponse.stream().anyMatch(g -> g.getName().equals(branch))) {
                    badgeStatus = new BadgeStatus(LATEST, commit_sha_short);
                } else {
                    badgeStatus = new BadgeStatus(OUTDATED, commit_sha_short);
                }

                return badgeStatus;

            } else {
                return new BadgeStatus(ERROR, gitHubResponseEntity.getStatusCode().getReasonPhrase());
            }
        } catch (final HttpClientErrorException.Forbidden f) {
            final HttpHeaders h = f.getResponseHeaders();
            log.warn("Got rate limited by github: " + f.getLocalizedMessage() + ", " + safeHeadersPrint.apply(h));
            return new BadgeStatus(ERROR, "Github:" + f.getStatusText());
        } catch (final HttpClientErrorException f) {
            log.error("Github: " + f.getLocalizedMessage());
            return new BadgeStatus(ERROR, "Github:" + f.getStatusText());
        } catch (final Exception e) {
            log.error("Github", e);
            return new BadgeStatus(ERROR, "Github:" + e.getLocalizedMessage());
        } finally {
            log.info("Github: " + owner + "/" + repo + ", branch=" + branch + ", sha=" + commit_sha + ", duration=" + abs(System.currentTimeMillis() - startTime) + " msec, " + safeHeadersPrint.apply(responseHeaders));
        }

    }

    private String getKey(final String owner, final String repo, final String branch, final String commit_sha) {
        return owner + "/" + repo + "/" + branch + "/" + commit_sha;
    }

    private String getToken() {

        final String token = System.getenv("GITHUB_TOKEN");

        if (StringUtils.isEmpty(token)) {
            log.error("Empty GITHUB_TOKEN");
        }

        return token;

    }

}
