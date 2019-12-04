package nl.appsource.latest.badge.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.latest.badge.actual.Actuator;
import nl.appsource.latest.badge.expected.GitHub;
import nl.appsource.latest.badge.model.shieldsio.ShieldsIoResponse;
import nl.appsource.latest.badge.output.ShieldsIo;
import nl.appsource.latest.badge.output.Svg;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@Slf4j
@RequiredArgsConstructor
public class BadgeController {

    private final GitHub gitHub;
    private final Actuator actuator;
    private final Svg svg;
    private final ShieldsIo shieldsIo;

    @GetMapping(value = "/github/sha/{owner}/{repo}/{branch}/{commit_sha}/badge.svg", produces = {"image/svg+xml;charset=utf-8"})
    public ResponseEntity<String> badgeGitHub(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @PathVariable("commit_sha") final String commit_sha) {

        if (log.isDebugEnabled()) {
            log.debug("owner=" + owner + ", repo=" + repo + ", branch=" + branch + ", commit_sha=" + commit_sha);
        }

        try {
            final BadgeStatus badgeStatus = gitHub.getLatestStatus(owner, repo, branch, commit_sha);
            final String image = svg.create(badgeStatus);
            return ResponseEntity.ok(image);
        } catch (final BadgeException badgeException) {
            return ResponseEntity.ok(svg.create(badgeException.getBadgeStatus()));
        }

    }

    @GetMapping(value = "/github/actuator/{owner}/{repo}/{branch}/badge.svg", produces = {"image/svg+xml;charset=utf-8"})
    public ResponseEntity<String> badgeActuator(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @RequestParam(value = "actuator_url") final String actuator_url) {

        if (log.isDebugEnabled()) {
            log.debug("owner=" + owner + ", repo=" + repo + ", branch=" + branch + ", actuator_url=" + actuator_url);
        }

        try {
            final String commit_sha = actuator.getCommitSha(actuator_url);
            final BadgeStatus badgeStatus = gitHub.getLatestStatus(owner, repo, branch, commit_sha);
            final String image = svg.create(badgeStatus);
            return ResponseEntity.ok(image);
        } catch (final BadgeException badgeException) {
            return ResponseEntity.ok(svg.create(badgeException.getBadgeStatus()));
        }

    }

    @ResponseBody
    @GetMapping(value = "/github/sha/{owner}/{repo}/{branch}/{commit_sha}", produces = APPLICATION_JSON_VALUE)
    public ShieldsIoResponse shieldsIoGitHub(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @PathVariable("commit_sha") final String commit_sha) {

        if (log.isDebugEnabled()) {
            log.debug("owner=" + owner + ", repo=" + repo + ", branch=" + branch + ", commit_sha=" + commit_sha);
        }

        try {
            final BadgeStatus status = gitHub.getLatestStatus(owner, repo, branch, commit_sha);
            return shieldsIo.create(status);
        } catch (final BadgeException badgeException) {
            return shieldsIo.create(badgeException.getBadgeStatus());
        }
    }

    @ResponseBody
    @GetMapping(value = "/github/actuator/{owner}/{repo}/{branch}", produces = APPLICATION_JSON_VALUE)
    public ShieldsIoResponse shieldsIoActuator(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @RequestParam(value = "actuator_url") final String actuator_url) {

        if (log.isDebugEnabled()) {
            log.debug("owner=" + owner + ", repo=" + repo + ", branch=" + branch + ", actuator_url=" + actuator_url);
        }

        try {
            final String commit_sha = actuator.getCommitSha(actuator_url);
            final BadgeStatus status = gitHub.getLatestStatus(owner, repo, branch, commit_sha);
            return shieldsIo.create(status);
        } catch (final BadgeException badgeException) {
            return shieldsIo.create(badgeException.getBadgeStatus());
        }

    }

}
