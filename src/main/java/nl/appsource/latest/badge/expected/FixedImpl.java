package nl.appsource.latest.badge.expected;

import lombok.extern.slf4j.Slf4j;
import nl.appsource.latest.badge.controller.BadgeException;
import nl.appsource.latest.badge.controller.BadgeStatus;
import org.springframework.util.StringUtils;

import static nl.appsource.latest.badge.controller.BadgeStatus.Status.ERROR;
import static nl.appsource.latest.badge.controller.BadgeStatus.Status.LATEST;
import static nl.appsource.latest.badge.controller.BadgeStatus.Status.OUTDATED;

@Slf4j
public class FixedImpl implements Fixed {

    @Override
    public BadgeStatus getBadgeStatus(final String expected, final String actual) throws BadgeException {

        if (StringUtils.hasText(expected)) {
            throw new BadgeException(new BadgeStatus(ERROR, "expected is null"));
        }

        if (StringUtils.hasText(actual)) {
            throw new BadgeException(new BadgeStatus(ERROR, "actual is null"));
        }

        try {
            return new BadgeStatus(actual.equals(expected) ? LATEST : OUTDATED, actual);
        } catch (Exception e) {
            throw new BadgeException(new BadgeStatus(ERROR, e.getLocalizedMessage()));
        }

    }
}
