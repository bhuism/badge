package nl.appsource.latest.badge.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.latest.badge.model.actuator.Info;
import nl.appsource.latest.badge.model.github.GitHubResponse;
import nl.appsource.latest.badge.model.shieldsio.ShieldsIoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class BadgeController {

    private static final String OWNER = "owner";
    private static final String REPO = "repo";
    private static final String COMMIT_SHA = "commit_sha";

    private static final String GIT_BRANCHE_WHERE_HEAD_URL = "https://api.github.com/repos/{" + OWNER + "}/{" + REPO + "}/commits/{" + COMMIT_SHA + "}/branches-where-head";

    private static final MediaType GITHUB_PREVIEW_MEDIATYPE = MediaType.valueOf("application/vnd.github.groot-preview+json");

    private final RestTemplate restTemplate;

    @GetMapping("/")
    public ModelAndView redirectWithUsingRedirectPrefix(final ModelMap model) {
        return new ModelAndView("redirect:https://github.com/bhuism/badge", model);
    }

    @Value("classpath:/info.json")
    private Resource index;

    @Profile("production")
    @GetMapping(value = "/actuator/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity info() throws IOException {
        return ResponseEntity.ok(new InputStreamResource(index.getInputStream()));
    }

    @Profile("production")
    @GetMapping(value = "/actuator/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity health() {
        return ResponseEntity.ok("{\"status\":\"UP\"}");
    }

    @ResponseBody
    @GetMapping(value = "/github/actuator/{owner}/{repo}/{branch}")
    public ShieldsIoResponse actuator(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @RequestParam(value = "actuator_url", required = true) final String actuator_url, @RequestParam(name = "label", required = false) String label) {

        if (log.isDebugEnabled()) {
            log.debug("owner=" + owner + ", repo=" + repo + ", branch=" + branch + ", actuator_url=" + actuator_url + ", label=" + label);
        }

        try {
            final String commit_sha = getCommitShaFromActuatorUrl(actuator_url);
            return calcSieldIoResponse(owner, repo, branch, commit_sha, label);
        } catch (Exception e) {
            log.info("", e);
            final ShieldsIoResponse shieldsIoResponse = new ShieldsIoResponse();
            shieldsIoResponse.setLabel("exception:");
            shieldsIoResponse.setMessage(e.getMessage());
            shieldsIoResponse.setColor("red");
            return shieldsIoResponse;
        }

    }

    @ResponseBody
    @GetMapping(value = "/github/sha/{owner}/{repo}/{branch}/{commit_sha}")
    public ShieldsIoResponse commit_sha(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @PathVariable("commit_sha") final String commit_sha, @RequestParam(name = "label", required = false) String label) {

        if (log.isDebugEnabled()) {
            log.debug("owner=" + owner + ", repo=" + repo + ", branch=" + branch + ", commit_sha=" + commit_sha + ", label=" + label);
        }

        try {
            return calcSieldIoResponse(owner, repo, branch, commit_sha, label);
        } catch (Exception e) {
            log.info("", e);
            final ShieldsIoResponse shieldsIoResponse = new ShieldsIoResponse();
            shieldsIoResponse.setLabel("exception:");
            shieldsIoResponse.setMessage(e.getMessage());
            shieldsIoResponse.setColor("red");
            return shieldsIoResponse;
        }

    }

    private String getCommitShaFromActuatorUrl(final String actuator_url) {

        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        final ResponseEntity<Info> info = restTemplate.exchange(actuator_url, HttpMethod.GET, new HttpEntity<>(headers), Info.class);

        if (info.getStatusCode().equals(HttpStatus.OK)) {
            return info.getBody().getGit().getCommit().getId();
        } else {
            return info.getStatusCode().name();
        }

    }

    private ShieldsIoResponse calcSieldIoResponse(final String owner, final String repo, final String branch, final String commit_sha, final String label) {

        final ShieldsIoResponse shieldsIoResponse = new ShieldsIoResponse();

        if (StringUtils.hasText(label)) {
            shieldsIoResponse.setLabel(label);
        }

        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(GITHUB_PREVIEW_MEDIATYPE));

        final Map<String, String> vars = new HashMap<>();

        vars.put(OWNER, owner);
        vars.put(REPO, repo);
        vars.put(COMMIT_SHA, commit_sha);

        final ResponseEntity<GitHubResponse[]> gitHubResponseEntity = restTemplate.exchange(GIT_BRANCHE_WHERE_HEAD_URL, HttpMethod.GET, new HttpEntity<>(headers), GitHubResponse[].class, vars);

        final String commit_sha_short = commit_sha.substring(0, Math.min(commit_sha.length(), 7));

        shieldsIoResponse.setMessage(commit_sha_short);

        if (gitHubResponseEntity.getStatusCode().equals(HttpStatus.OK)) {

            final List<GitHubResponse> gitHubResponse = Arrays.asList(gitHubResponseEntity.getBody());

            if (gitHubResponse.stream().anyMatch(g -> {
                return g.getName().equals(branch);
            })) {
                shieldsIoResponse.setColor("green");
            } else {
                shieldsIoResponse.setColor("orange");
            }

        } else {
            shieldsIoResponse.setLabel("github:");
            shieldsIoResponse.setMessage(gitHubResponseEntity.getStatusCode().getReasonPhrase());
            shieldsIoResponse.setColor("red");
            shieldsIoResponse.setIsError(true);
        }

        return shieldsIoResponse;

    }

}
