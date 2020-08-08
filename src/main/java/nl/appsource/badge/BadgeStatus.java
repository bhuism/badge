package nl.appsource.badge;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@Getter
@RequiredArgsConstructor(access = PRIVATE)
public class BadgeStatus {

    @Getter
    @RequiredArgsConstructor
    public enum Status {

        ERROR("error", "#555", "red"),
        OUTDATED("outdated", "#555", "orange"),
        LATEST("latest", "#555", "#4c1");

        private final String labelText;
        private final String labelColor;
        private final String messageColor;

    }

    private final Status status;
    private final String messageText;

    public static BadgeStatus ofError(final String message) {
        return new BadgeStatus(Status.ERROR, message);
    }

    public static BadgeStatus ofOutDated(final String message) {
        return new BadgeStatus(Status.OUTDATED, message);
    }

    public static BadgeStatus ofLatest(final String message) {
        return new BadgeStatus(Status.LATEST, message);
    }

}
