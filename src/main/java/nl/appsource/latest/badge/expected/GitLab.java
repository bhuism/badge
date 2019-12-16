package nl.appsource.latest.badge.expected;

import nl.appsource.latest.badge.controller.BadgeException;
import nl.appsource.latest.badge.controller.BadgeStatus;

public interface GitLab {

    BadgeStatus getBadgeStatus(final String id, final String branch, final String commit_sha) throws BadgeException;

}
