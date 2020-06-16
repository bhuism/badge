package nl.appsource.latest.badge.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.latest.badge.actual.Actuator;
import nl.appsource.latest.badge.expected.Fixed;
import nl.appsource.latest.badge.expected.GitHub;
import nl.appsource.latest.badge.expected.GitLab;
import nl.appsource.latest.badge.model.shieldsio.ShieldsIoResponse;
import nl.appsource.latest.badge.output.ShieldsIo;
import nl.appsource.latest.badge.output.Svg;
import org.springframework.http.HttpHeaders;
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
    private final GitLab gitLab;
    private final Fixed fixed;
    private final Actuator actuator;
    private final Svg svg;
    private final ShieldsIo shieldsIo;

    private static class DoNotCache {
        public static ResponseEntity<String> ok(String image) {
            return ResponseEntity.ok()
                    .headers((header) -> {
                        header.set(HttpHeaders.EXPIRES, "0");
                        header.setPragma("no-cache");
                        header.setCacheControl("no-cache, no-store, max-age=0, must-revalidate");
                    }).body(image);
        }
    }

    @GetMapping(value = "/gitlab/sha/{id}/{branch}/{commit_sha}/badge.svg", produces = {"image/svg+xml;charset=utf-8"})
    public ResponseEntity<String> badgeGitLab(@PathVariable("id") final String id, @PathVariable("branch") final String branch, @PathVariable("commit_sha") final String commit_sha) {

        try {
            final BadgeStatus badgeStatus = gitLab.getBadgeStatus(id, branch, commit_sha);
            final String image = svg.create(badgeStatus);
            return DoNotCache.ok(image);
        } catch (final BadgeException badgeException) {
            return DoNotCache.ok(svg.create(badgeException.getBadgeStatus()));
        }

    }

    @GetMapping(value = "/gitlab/actuator/{id}/{branch}/badge.svg", produces = {"image/svg+xml;charset=utf-8"})
    public ResponseEntity<String> badgeGitLabActuator(@PathVariable("id") final String id, @PathVariable("branch") final String branch, @RequestParam(value = "actuator_url") final String actuator_url) {

        try {
            final String commit_sha = actuator.getCommitSha(actuator_url);
            final BadgeStatus badgeStatus = gitLab.getBadgeStatus(id, branch, commit_sha);
            final String image = svg.create(badgeStatus);
            return DoNotCache.ok(image);
        } catch (final BadgeException badgeException) {
            return DoNotCache.ok(svg.create(badgeException.getBadgeStatus()));
        }

    }


    @GetMapping(value = "/github/sha/{owner}/{repo}/{branch}/{commit_sha}/badge.svg", produces = {"image/svg+xml;charset=utf-8"})
    public ResponseEntity<String> badgeGitHub(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @PathVariable("commit_sha") final String commit_sha) {

        try {
            final BadgeStatus badgeStatus = gitHub.getBadgeStatus(owner, repo, branch, commit_sha);
            final String image = svg.create(badgeStatus);
            return DoNotCache.ok(image);
        } catch (final BadgeException badgeException) {
            return DoNotCache.ok(svg.create(badgeException.getBadgeStatus()));
        }

    }

    @GetMapping(value = "/github/actuator/{owner}/{repo}/{branch}/badge.svg", produces = {"image/svg+xml;charset=utf-8"})
    public ResponseEntity<String> badgeGitHubActuator(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @RequestParam(value = "actuator_url") final String actuator_url) {

        try {
            final String commit_sha = actuator.getCommitSha(actuator_url);
            final BadgeStatus badgeStatus = gitHub.getBadgeStatus(owner, repo, branch, commit_sha);
            final String image = svg.create(badgeStatus);
            return DoNotCache.ok(image);
        } catch (final BadgeException badgeException) {
            return DoNotCache.ok(svg.create(badgeException.getBadgeStatus()));
        }

    }

    @ResponseBody
    @GetMapping(value = "/github/sha/{owner}/{repo}/{branch}/{commit_sha}", produces = APPLICATION_JSON_VALUE)
    public ShieldsIoResponse shieldsIoGitHub(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @PathVariable("commit_sha") final String commit_sha) {

        try {
            final BadgeStatus status = gitHub.getBadgeStatus(owner, repo, branch, commit_sha);
            return shieldsIo.create(status);
        } catch (final BadgeException badgeException) {
            return shieldsIo.create(badgeException.getBadgeStatus());
        }
    }

    @ResponseBody
    @GetMapping(value = "/github/actuator/{owner}/{repo}/{branch}", produces = APPLICATION_JSON_VALUE)
    public ShieldsIoResponse shieldsIoActuator(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @RequestParam(value = "actuator_url") final String actuator_url) {

        try {
            final String commit_sha = actuator.getCommitSha(actuator_url);
            final BadgeStatus status = gitHub.getBadgeStatus(owner, repo, branch, commit_sha);
            return shieldsIo.create(status);
        } catch (final BadgeException badgeException) {
            return shieldsIo.create(badgeException.getBadgeStatus());
        }

    }

    @ResponseBody
    @GetMapping(value = "/fixed/actuator/{latest}", produces = APPLICATION_JSON_VALUE)
    public ShieldsIoResponse shieldsIoActuator(@PathVariable("latest") final String latest, @RequestParam(value = "actuator_url") final String actuator_url) {

        try {
            final String actual = actuator.getCommitSha(actuator_url);
            final BadgeStatus status = fixed.getBadgeStatus(latest, actual);
            return shieldsIo.create(status);
        } catch (final BadgeException badgeException) {
            return shieldsIo.create(badgeException.getBadgeStatus());
        }

    }

}
