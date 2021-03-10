package nl.appsource.badge.service;

import nl.appsource.badge.model.shieldsio.ShieldsIoResponse;

public interface BadgeController {

    String badgeGitLab(String id, String branch, String commit_sha);

    String badgeGitLabActuator(String id, String branch, String actuator_url);

    ShieldsIoResponse shieldsIoGitLab(String id, String branch, String commit_sha);

    ShieldsIoResponse shieldsIoGitLabActuator(String id, String branch, String actuator_url);

    String badgeGitHub(String owner, String repo, String branch, String commit_sha);

    String badgeGitHubActuator(String owner, String repo, String branch, String actuator_url);

    ShieldsIoResponse shieldsIoGitHub(String owner, String repo, String branch, String commit_sha);

    ShieldsIoResponse shieldsIoGitHubActuator(String owner, String repo, String branch, String actuator_url);

    ShieldsIoResponse shieldsIoActuator(String latest, String actuator_url);

    String badgeGitHubHtmlUrl(String owner, String repo, String branch, String htmlUrl);

}
