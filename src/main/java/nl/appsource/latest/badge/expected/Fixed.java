package nl.appsource.latest.badge.expected;

import nl.appsource.latest.badge.controller.BadgeException;
import nl.appsource.latest.badge.controller.BadgeStatus;

public interface Fixed {

    BadgeStatus getBadgeStatus(final String latest, final String actual) throws BadgeException;

}
