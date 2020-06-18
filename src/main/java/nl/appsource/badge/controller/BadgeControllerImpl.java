package nl.appsource.badge.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.badge.actual.Actuator;
import nl.appsource.badge.expected.Fixed;
import nl.appsource.badge.expected.GitHub;
import nl.appsource.badge.expected.GitLab;
import nl.appsource.badge.model.shieldsio.ShieldsIoResponse;
import nl.appsource.badge.output.ShieldsIo;
import nl.appsource.badge.output.Svg;

@Slf4j
@RequiredArgsConstructor
public class BadgeControllerImpl implements BadgeController {

    private final GitHub gitHub;
    private final GitLab gitLab;
    private final Fixed fixed;
    private final Actuator actuator;
    private final Svg svg;
    private final ShieldsIo shieldsIo;

    @Override
    public String badgeGitLab(final String id, final String branch, final String commit_sha) {

        try {
            final BadgeStatus badgeStatus = gitLab.getBadgeStatus(id, branch, commit_sha);
            return svg.create(badgeStatus);
        } catch (final BadgeException badgeException) {
            return svg.create(badgeException.getBadgeStatus());
        }

    }

    @Override
    public String badgeGitLabActuator(final String id, final String branch, final String actuator_url) {

        try {
            final String commit_sha = actuator.getCommitSha(actuator_url);
            final BadgeStatus badgeStatus = gitLab.getBadgeStatus(id, branch, commit_sha);
            return svg.create(badgeStatus);
        } catch (final BadgeException badgeException) {
            return svg.create(badgeException.getBadgeStatus());
        }

    }


    @Override
    public String badgeGitHub(final String owner, final String repo, final String branch, final String commit_sha) {

        try {
            final BadgeStatus badgeStatus = gitHub.getBadgeStatus(owner, repo, branch, commit_sha);
            return svg.create(badgeStatus);
        } catch (final BadgeException badgeException) {
            return svg.create(badgeException.getBadgeStatus());
        }

    }

    @Override
    public String badgeGitHubActuator(final String owner, final String repo, final String branch, final String actuator_url) {

        try {
            final String commit_sha = actuator.getCommitSha(actuator_url);
            final BadgeStatus badgeStatus = gitHub.getBadgeStatus(owner, repo, branch, commit_sha);
            return svg.create(badgeStatus);
        } catch (final BadgeException badgeException) {
            return svg.create(badgeException.getBadgeStatus());
        }

    }

    @Override
    public ShieldsIoResponse shieldsIoGitHub(final String owner, final String repo, final String branch, final String commit_sha) {

        try {
            final BadgeStatus status = gitHub.getBadgeStatus(owner, repo, branch, commit_sha);
            return shieldsIo.create(status);
        } catch (final BadgeException badgeException) {
            return shieldsIo.create(badgeException.getBadgeStatus());
        }
    }

    @Override
    public ShieldsIoResponse shieldsIoActuator(final String owner, final String repo, final String branch, final String actuator_url) {

        try {
            final String commit_sha = actuator.getCommitSha(actuator_url);
            final BadgeStatus status = gitHub.getBadgeStatus(owner, repo, branch, commit_sha);
            return shieldsIo.create(status);
        } catch (final BadgeException badgeException) {
            return shieldsIo.create(badgeException.getBadgeStatus());
        }

    }

    @Override
    public ShieldsIoResponse shieldsIoActuator(final String latest, final String actuator_url) {

        try {
            final String actual = actuator.getCommitSha(actuator_url);
            final BadgeStatus status = fixed.getBadgeStatus(latest, actual);
            return shieldsIo.create(status);
        } catch (final BadgeException badgeException) {
            return shieldsIo.create(badgeException.getBadgeStatus());
        }

    }

}
