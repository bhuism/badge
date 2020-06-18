package nl.appsource.badge.expected;

import nl.appsource.badge.controller.BadgeException;
import nl.appsource.badge.controller.BadgeStatus;

public interface GitLab {

    BadgeStatus getBadgeStatus(final String id, final String branch, final String commit_sha) throws BadgeException;

}
