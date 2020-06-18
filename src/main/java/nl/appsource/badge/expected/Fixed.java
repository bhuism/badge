package nl.appsource.badge.expected;

import nl.appsource.badge.controller.BadgeException;
import nl.appsource.badge.controller.BadgeStatus;

public interface Fixed {

    BadgeStatus getBadgeStatus(final String latest, final String actual) throws BadgeException;

}
