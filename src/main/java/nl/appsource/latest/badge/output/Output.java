package nl.appsource.latest.badge.output;

import nl.appsource.latest.badge.controller.BadgeStatus;

public interface Output<T> {

    T create (final BadgeStatus badgeStatus, final String message, final String label);

}
