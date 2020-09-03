package nl.appsource.badge.expected;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.badge.BadgeException;
import nl.appsource.badge.model.github.GitHubResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.lang.Math.abs;
import static java.util.stream.Collectors.joining;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
public class GitHubImpl implements GitHub {

    private static final String OWNER = "owner";
    private static final String REPO = "repo";
    private static final String BRANCHE = "branche";
    private static final String GIT_BRANCHES_WHERE_HEAD_URL = "https://api.github.com/repos/{" + OWNER + "}/{" + REPO + "}/commits/{" + BRANCHE + "}";
    private static final MediaType GITHUB_PREVIEW_MEDIATYPE = MediaType.valueOf("application/vnd.github.groot-preview+json");

    private static final String LIMIT = "X-RateLimit-Limit";
    private static final String REMAINING = "X-RateLimit-Remaining";
    private static final String RESET = "X-RateLimit-Reset";

    private final RestTemplate restTemplate;

    private final BiFunction<HttpHeaders, String, String> safeHeaderPrint = (responseHeaders, key) ->
        responseHeaders == null ? null :
            key + "=" + Optional.ofNullable(responseHeaders.get(key))
                .map(Collection::stream)
                .flatMap(Stream::findFirst)
                .orElse(null);

    private final Function<HttpHeaders, String> safeHeadersPrint = (responseHeaders) ->
        Stream.of(LIMIT, REMAINING, RESET).map(key -> safeHeaderPrint.apply(responseHeaders, key)).collect(joining(", "));

    @Override
    public String apply(final GitHub.GitHubKey gitHubKey) throws BadgeException {

        final long startTime = System.currentTimeMillis();

        final String owner = gitHubKey.getOwner();
        final String repo = gitHubKey.getRepo();
        final String branch = gitHubKey.getBranch();

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
            vars.put(BRANCHE, branch);

            final ResponseEntity<GitHubResponse> gitHubResponseEntity = restTemplate.exchange(GIT_BRANCHES_WHERE_HEAD_URL, HttpMethod.GET, new HttpEntity<>(requestHeaders), GitHubResponse.class, vars);

            responseHeaders = gitHubResponseEntity.getHeaders();

            if (gitHubResponseEntity.getBody() != null && gitHubResponseEntity.getStatusCode().equals(HttpStatus.OK)) {
                return gitHubResponseEntity.getBody().getSha();
            } else {
                throw new BadgeException(gitHubResponseEntity.getStatusCode().getReasonPhrase());
            }
        } catch (final HttpClientErrorException.Forbidden f) {
            final HttpHeaders h = f.getResponseHeaders();
            log.warn("Got rate limited by github: " + f.getLocalizedMessage() + ", " + safeHeadersPrint.apply(h));
            throw new BadgeException("Github:" + f.getStatusText());
        } catch (final HttpClientErrorException f) {
            log.error("Github: " + f.getLocalizedMessage());
            throw new BadgeException("Github:" + f.getStatusText());
        } catch (final Exception e) {
            log.error("Github", e);
            throw new BadgeException("Github:" + e.getMessage());
        } finally {
            log.info("Github: " + owner + "/" + repo + ", branch=" + branch + ", duration=" + abs(System.currentTimeMillis() - startTime) + " msec, " + safeHeadersPrint.apply(responseHeaders));
        }

    }

    private String getToken() {

        final String token = System.getenv("GITHUB_TOKEN");

        if (StringUtils.isEmpty(token)) {
            log.error("Empty GITHUB_TOKEN");
        }

        return token;

    }

}
