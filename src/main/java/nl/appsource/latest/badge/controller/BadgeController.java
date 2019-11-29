package nl.appsource.latest.badge.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.latest.badge.lib.Widths;
import nl.appsource.latest.badge.model.actuator.Info;
import nl.appsource.latest.badge.model.github.GitHubResponse;
import nl.appsource.latest.badge.model.shieldsio.ShieldsIoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;

@Controller
@Slf4j
@RequiredArgsConstructor
public class BadgeController {

    private static final String OWNER = "owner";
    private static final String REPO = "repo";
    private static final String COMMIT_SHA = "commit_sha";

    private static final String GIT_BRANCHES_WHERE_HEAD_URL = "https://api.github.com/repos/{" + OWNER + "}/{" + REPO + "}/commits/{" + COMMIT_SHA + "}/branches-where-head";

    private static final MediaType GITHUB_PREVIEW_MEDIATYPE = MediaType.valueOf("application/vnd.github.groot-preview+json");

    private final RestTemplate restTemplate;

    @Value("classpath:/info.json")
    private Resource index;

    @Value("classpath:/template.svg")
    private Resource templateSvg;

    private String template;

    @PostConstruct
    private void postConstruct() throws IOException {
        template = FileCopyUtils.copyToString(new InputStreamReader(templateSvg.getInputStream(), UTF_8));
    }

    @GetMapping(value = "/actuator/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InputStreamResource> info() throws IOException {
        return ResponseEntity.ok(new InputStreamResource(index.getInputStream()));
    }

    @GetMapping(value = "/actuator/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":\"UP\"}");
    }

    @GetMapping(value = "/github/actuator/{owner}/{repo}/{branch}", consumes = MediaType.ALL_VALUE, produces = {"image/svg+xml"})
    public ResponseEntity<String> badgeActuator(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @RequestParam(value = "actuator_url") final String actuator_url, @RequestParam(name = "label", required = false) String label) {

        if (log.isDebugEnabled()) {
            log.debug("owner=" + owner + ", repo=" + repo + ", branch=" + branch + ", actuator_url=" + actuator_url + ", label=" + label);
        }

        final ShieldsIoResponse shieldsIoResponse = this.shieldsIoActuator(owner, repo, branch, actuator_url, label);
        final String image = createImageFromShieldsIo(shieldsIoResponse);
        return ResponseEntity.ok(image);

    }

    @ResponseBody
    @GetMapping(value = "/github/actuator/{owner}/{repo}/{branch}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ShieldsIoResponse shieldsIoActuator(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @RequestParam(value = "actuator_url") final String actuator_url, @RequestParam(name = "label", required = false) String label) {

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

    @GetMapping(value = "/github/sha/{owner}/{repo}/{branch}/{commit_sha}", consumes = MediaType.ALL_VALUE, produces = {"image/svg+xml"})
    public ResponseEntity<String> badgeCommit_sha(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @PathVariable("commit_sha") final String commit_sha, @RequestParam(name = "label", required = false) String label) {

        if (log.isDebugEnabled()) {
            log.debug("owner=" + owner + ", repo=" + repo + ", branch=" + branch + ", commit_sha=" + commit_sha + ", label=" + label);
        }

        final ShieldsIoResponse shieldsIoResponse = this.shieldsIoCommit_sha(owner, repo, branch, commit_sha, label);
        final String image = createImageFromShieldsIo(shieldsIoResponse);
        return ResponseEntity.ok(image);

    }

    private String createImageFromShieldsIo(final ShieldsIoResponse shieldsIoResponse) {
        return createImage(shieldsIoResponse.getLabel(), shieldsIoResponse.getMessage(), shieldsIoResponse.getLabelColor(), shieldsIoResponse.getIsError() ? "red" : shieldsIoResponse.getColor());
    }

    private String createImage(final String labelText, final String messageText, final String labelColor, final String messageColor) {

        final Properties properties = new Properties();

//        final String labelText = shieldsIoResponse.getLabel();
//        final String messageText = shieldsIoResponse.getMessage();

        double leftTextWidth = Widths.getWidthOfString(labelText) / 10.0;
        double rightTextWidth = Widths.getWidthOfString(messageText) / 10.0;

        double leftWidth = leftTextWidth + 10 + 14 + 3;
        double rightWidth = rightTextWidth + 10;

        final double totalWidth = leftWidth + rightWidth;

        final int logoWidth = 14;
        final int logoPadding = 3;

        properties.put("labelText", labelText);
        properties.put("messageText", messageText);
        properties.put("totalWidth", "" + totalWidth);
        properties.put("labelWidth", "" + leftWidth);
        properties.put("messageWidth", "" + rightWidth);
        properties.put("labelBackgroudColor", labelColor);
        properties.put("messageBackgroudColor", messageColor);
        properties.put("logoWidth", "" + logoWidth);
        properties.put("labelTextX", "" + (((leftWidth + logoWidth + logoPadding) / 2.0) + 1) * 10);
        properties.put("labelTextLength", "" + (leftWidth - (10 + logoWidth + logoPadding)) * 10);
        properties.put("messageTextX", "" + ((leftWidth + rightWidth / 2.0) - 1) * 10);
        properties.put("messageTextLength", "" + (rightWidth - 10) * 10);

        return new PropertyPlaceholderHelper("${", "}", null, false).replacePlaceholders(template, properties);

    }

    @ResponseBody
    @GetMapping(value = "/github/sha/{owner}/{repo}/{branch}/{commit_sha}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ShieldsIoResponse shieldsIoCommit_sha(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @PathVariable("commit_sha") final String commit_sha, @RequestParam(name = "label", required = false) String label) {

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

        final String commit_sha_short = commit_sha.substring(0, Math.min(commit_sha.length(), 7));

        shieldsIoResponse.setMessage(commit_sha_short);

        final HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(GITHUB_PREVIEW_MEDIATYPE));

        final Map<String, String> vars = new HashMap<>();

        vars.put(OWNER, owner);
        vars.put(REPO, repo);
        vars.put(COMMIT_SHA, commit_sha);

        final ResponseEntity<GitHubResponse[]> gitHubResponseEntity = restTemplate.exchange(GIT_BRANCHES_WHERE_HEAD_URL, HttpMethod.GET, new HttpEntity<>(headers), GitHubResponse[].class, vars);

        if (gitHubResponseEntity.getStatusCode().equals(HttpStatus.OK)) {

            final List<GitHubResponse> gitHubResponse = Arrays.asList(gitHubResponseEntity.getBody());

            if (gitHubResponse.stream().anyMatch(g -> g.getName().equals(branch))) {
                shieldsIoResponse.setLabel("latest");
                shieldsIoResponse.setColor("#97ca00");
            } else {
                shieldsIoResponse.setLabel("outdated");
                shieldsIoResponse.setColor("orange");
            }

        } else {
            shieldsIoResponse.setLabel("github:");
            shieldsIoResponse.setMessage(gitHubResponseEntity.getStatusCode().getReasonPhrase());
            shieldsIoResponse.setColor("red");
            shieldsIoResponse.setIsError(true);
        }

        if (StringUtils.hasText(label)) {
            shieldsIoResponse.setLabel(label);
        }

        return shieldsIoResponse;

    }

}
