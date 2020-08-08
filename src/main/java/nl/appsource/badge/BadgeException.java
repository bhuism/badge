package nl.appsource.badge;

public class BadgeException extends RuntimeException {

    public BadgeException(final String message) {
        super(message);
    }

    public BadgeException(final String message, final Exception e) {
        super(message, e);
    }

    public BadgeStatus getBadgeStatus() {
        return BadgeStatus.ofError(getMessage());
    }

}
