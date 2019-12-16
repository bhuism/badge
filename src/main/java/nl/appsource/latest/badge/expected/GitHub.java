package nl.appsource.latest.badge.expected;

import nl.appsource.latest.badge.controller.BadgeException;
import nl.appsource.latest.badge.controller.BadgeStatus;

public interface GitHub {

    BadgeStatus getBadgeStatus(final String owner, final String repo, final String branch, final String commit_sha) throws BadgeException;

}
