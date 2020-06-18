package nl.appsource.badge.expected;

import nl.appsource.badge.controller.BadgeException;
import nl.appsource.badge.controller.BadgeStatus;

public interface GitHub {

    BadgeStatus getBadgeStatus(final String owner, final String repo, final String branch, final String commit_sha) throws BadgeException;

}
