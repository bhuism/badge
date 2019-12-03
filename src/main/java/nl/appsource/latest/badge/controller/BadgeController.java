package nl.appsource.latest.badge.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.latest.badge.client.Actuator;
import nl.appsource.latest.badge.client.GitHub;
import nl.appsource.latest.badge.lib.Widths;
import nl.appsource.latest.badge.model.shieldsio.ShieldsIoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
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

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.appsource.latest.badge.controller.BadgeStatus.ERROR;

@Controller
@Slf4j
@RequiredArgsConstructor
public class BadgeController {


    @Value("classpath:/info.json")
    private Resource index;

    @Value("classpath:/template.svg")
    private Resource templateSvg;

    private String template;

    private final GitHub gitHub;

    private final Actuator actuator;

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

    @GetMapping(value = "/github/sha/{owner}/{repo}/{branch}/{commit_sha}", consumes = MediaType.ALL_VALUE, produces = {"image/svg+xml"})
    public ResponseEntity<String> badgeGitHub(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @PathVariable("commit_sha") final String commit_sha, @RequestParam(name = "label", required = false) String label) {

        if (log.isDebugEnabled()) {
            log.debug("owner=" + owner + ", repo=" + repo + ", branch=" + branch + ", commit_sha=" + commit_sha + ", label=" + label);
        }

        final BadgeStatus badgeStatus = gitHub.getLatestStatus(owner, repo, branch, commit_sha);
        final String commit_sha_short = commit_sha.substring(0, Math.min(commit_sha.length(), 7));
        final String image = createImageFromBadgeStatus(badgeStatus, commit_sha_short, label);
        return ResponseEntity.ok(image);

    }

    @GetMapping(value = "/github/actuator/{owner}/{repo}/{branch}", consumes = MediaType.ALL_VALUE, produces = {"image/svg+xml"})
    public ResponseEntity<String> badgeActuator(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @RequestParam(value = "actuator_url") final String actuator_url, @RequestParam(name = "label", required = false) String label) {

        if (log.isDebugEnabled()) {
            log.debug("owner=" + owner + ", repo=" + repo + ", branch=" + branch + ", actuator_url=" + actuator_url + ", label=" + label);
        }

        final String commit_sha = actuator.getCommitSha(actuator_url);
        final String commit_sha_short = commit_sha.substring(0, Math.min(commit_sha.length(), 7));
        final BadgeStatus badgeStatus = gitHub.getLatestStatus(owner, repo, branch, commit_sha);
        final String image = createImageFromBadgeStatus(badgeStatus, commit_sha_short, label);

        return ResponseEntity.ok(image);

    }

    @ResponseBody
    @GetMapping(value = "/github/sha/{owner}/{repo}/{branch}/{commit_sha}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ShieldsIoResponse shieldsIoGitHub(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @PathVariable("commit_sha") final String commit_sha, @RequestParam(name = "label", required = false) String label) {

        if (log.isDebugEnabled()) {
            log.debug("owner=" + owner + ", repo=" + repo + ", branch=" + branch + ", commit_sha=" + commit_sha + ", label=" + label);
        }

        final BadgeStatus status = gitHub.getLatestStatus(owner, repo, branch, commit_sha);
        final String commit_sha_short = commit_sha.substring(0, Math.min(commit_sha.length(), 7));
        return calcSieldIoResponse(status, commit_sha_short, label);
    }

    @ResponseBody
    @GetMapping(value = "/github/actuator/{owner}/{repo}/{branch}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ShieldsIoResponse shieldsIoActuator(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @RequestParam(value = "actuator_url") final String actuator_url, @RequestParam(name = "label", required = false) String label) {

        if (log.isDebugEnabled()) {
            log.debug("owner=" + owner + ", repo=" + repo + ", branch=" + branch + ", actuator_url=" + actuator_url + ", label=" + label);
        }

        final String commit_sha = actuator.getCommitSha(actuator_url);
        final BadgeStatus status = gitHub.getLatestStatus(owner, repo, branch, commit_sha);
        final String commit_sha_short = commit_sha.substring(0, Math.min(commit_sha.length(), 7));
        return calcSieldIoResponse(status, commit_sha_short, label);

    }

    private String createImageFromBadgeStatus(final BadgeStatus badgeStatus, final String message, final String label) {
        return createImage(StringUtils.hasText(label) ? label : badgeStatus.getLabelText(), message, badgeStatus.getLabelColor(), badgeStatus.getMessageColor());
    }

    private ShieldsIoResponse calcSieldIoResponse(final BadgeStatus status, final String message, final String label) {

        final ShieldsIoResponse shieldsIoResponse = new ShieldsIoResponse();

        shieldsIoResponse.setMessage(message);
        shieldsIoResponse.setLabel(StringUtils.hasText(label) ? label : status.getLabelText());
        shieldsIoResponse.setLabelColor(status.getLabelColor());
        shieldsIoResponse.setColor(status.getMessageColor());
        shieldsIoResponse.setIsError(ERROR.equals(status));

        //shieldsIoResponse.setMessage(gitHubResponseEntity.getStatusCode().getReasonPhrase());

        return shieldsIoResponse;

    }

    private String createImage(final String labelText, final String messageText, final String labelColor, final String messageColor) {

        final Properties properties = new Properties();

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
}
