package nl.appsource.badge.output;

import nl.appsource.badge.controller.BadgeStatus;

public interface Output<T> {

    T create(final BadgeStatus badgeStatus);

}
