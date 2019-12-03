package nl.appsource.latest.badge.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.latest.badge.actual.Actuator;
import nl.appsource.latest.badge.expected.GitHub;
import nl.appsource.latest.badge.model.shieldsio.ShieldsIoResponse;
import nl.appsource.latest.badge.output.ShieldsIo;
import nl.appsource.latest.badge.output.Svg;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
@RequiredArgsConstructor
public class BadgeController {

    private final GitHub gitHub;
    private final Actuator actuator;
    private final Svg svg;
    private final ShieldsIo shieldsIo;

    @GetMapping(value = "/github/sha/{owner}/{repo}/{branch}/{commit_sha}/badge.svg", consumes = MediaType.ALL_VALUE, produces = {"image/svg+xml"})
    public ResponseEntity<String> badgeGitHub(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @PathVariable("commit_sha") final String commit_sha, @RequestParam(name = "label", required = false) String label) {

        if (log.isDebugEnabled()) {
            log.debug("owner=" + owner + ", repo=" + repo + ", branch=" + branch + ", commit_sha=" + commit_sha + ", label=" + label);
        }

        final BadgeStatus badgeStatus = gitHub.getLatestStatus(owner, repo, branch, commit_sha);
        final String commit_sha_short = commit_sha.substring(0, Math.min(commit_sha.length(), 7));
        final String image = svg.create(badgeStatus, commit_sha_short, label);
        return ResponseEntity.ok(image);

    }

    @GetMapping(value = "/github/actuator/{owner}/{repo}/{branch}/badge.svg", consumes = MediaType.ALL_VALUE, produces = {"image/svg+xml"})
    public ResponseEntity<String> badgeActuator(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @RequestParam(value = "actuator_url") final String actuator_url, @RequestParam(name = "label", required = false) String label) {

        if (log.isDebugEnabled()) {
            log.debug("owner=" + owner + ", repo=" + repo + ", branch=" + branch + ", actuator_url=" + actuator_url + ", label=" + label);
        }

        final String commit_sha = actuator.getCommitSha(actuator_url);
        final BadgeStatus badgeStatus = gitHub.getLatestStatus(owner, repo, branch, commit_sha);
        final String commit_sha_short = commit_sha.substring(0, Math.min(commit_sha.length(), 7));
        final String image = svg.create(badgeStatus, commit_sha_short, label);

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
        return shieldsIo.create(status, commit_sha_short, label);
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
        return shieldsIo.create(status, commit_sha_short, label);

    }

}
