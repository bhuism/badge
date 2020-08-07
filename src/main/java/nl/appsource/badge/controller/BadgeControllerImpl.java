package nl.appsource.badge.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.badge.actual.Actuator;
import nl.appsource.badge.expected.GitHub;
import nl.appsource.badge.expected.GitLab;
import nl.appsource.badge.expected.GitLab.GitLabKey;
import nl.appsource.badge.model.shieldsio.ShieldsIoResponse;
import nl.appsource.badge.output.ShieldsIo;
import nl.appsource.badge.output.Svg;
import org.springframework.util.StringUtils;

import static nl.appsource.badge.BadgeApplication.cache;

@Slf4j
@RequiredArgsConstructor
public class BadgeControllerImpl implements BadgeController {

    private final GitHub gitHub;
    private final GitLab gitLab;
    private final Actuator actuator;
    private final Svg svg;
    private final ShieldsIo shieldsIo;

    @FunctionalInterface
    interface CatchBadgeException<T> {
        T get() throws BadgeException;
    }

    private String stringWithBadgeException(final CatchBadgeException<String> supplier) {
        try {
            return supplier.get();
        } catch (final BadgeException badgeException) {
            return svg.create(badgeException.getBadgeStatus());
        }
    }

    private ShieldsIoResponse shieldsIoWithBadgeException(final CatchBadgeException<ShieldsIoResponse> supplier) {
        try {
            return supplier.get();
        } catch (final BadgeException badgeException) {
            return shieldsIo.create(badgeException.getBadgeStatus());
        }
    }

    @Override
    public String badgeGitLab(final String id, final String branch, final String current) {
        return stringWithBadgeException(() -> {
            final String latest = cache.computeIfAbsent(new GitLabKey(id, branch), gitLab);
            return svg.create(calcBadeStatus(latest, current));
        });
    }

    @Override
    public String badgeGitLabActuator(final String id, final String branch, final String actuator_url) {
        return stringWithBadgeException(() -> {
            final String latest = cache.computeIfAbsent(new GitLabKey(id, branch), gitLab);
            final String current = cache.computeIfAbsent(actuator_url, actuator);
            return svg.create(calcBadeStatus(latest, current));
        });
    }


    @Override
    public String badgeGitHub(final String owner, final String repo, final String branch, final String current) {
        return stringWithBadgeException(() -> {
            final String latest = cache.computeIfAbsent(new GitHub.GitHubKey(owner, repo, branch), gitHub);
            return svg.create(calcBadeStatus(latest, current));
        });
    }

    @Override
    public String badgeGitHubActuator(final String owner, final String repo, final String branch, final String actuator_url) {
        return stringWithBadgeException(() -> {
            final String latest = cache.computeIfAbsent(new GitHub.GitHubKey(owner, repo, branch), gitHub);
            final String current = cache.computeIfAbsent(actuator_url, actuator);
            return svg.create(calcBadeStatus(latest, current));
        });
    }

    @Override
    public ShieldsIoResponse shieldsIoGitHub(final String owner, final String repo, final String branch, final String current) {
        return shieldsIoWithBadgeException(() -> {
            final String latest = cache.computeIfAbsent(new GitHub.GitHubKey(owner, repo, branch), gitHub);
            return shieldsIo.create(calcBadeStatus(latest, current));
        });
    }

    @Override
    public ShieldsIoResponse shieldsIoActuator(final String owner, final String repo, final String branch, final String actuator_url) {
        return shieldsIoWithBadgeException(() -> {
            final String latest = cache.computeIfAbsent(new GitHub.GitHubKey(owner, repo, branch), gitHub);
            final String current = cache.computeIfAbsent(actuator_url, actuator);
            return shieldsIo.create(calcBadeStatus(latest, current));
        });
    }

    @Override
    public ShieldsIoResponse shieldsIoActuator(final String latest, final String actuator_url) {
        return shieldsIoWithBadgeException(() -> {
            final String current = cache.computeIfAbsent(actuator_url, actuator);
            return shieldsIo.create(calcBadeStatus(latest, current));
        });
    }

    private BadgeStatus calcBadeStatus(final String _latest, final String _current) {
        if (StringUtils.hasText(_current) && _current.length() >= 7) {
            if (StringUtils.hasText(_latest) && _latest.length() >= 7) {

                final String latest = _latest.substring(0, 7);
                final String current = _current.substring(0, 7);

                if (latest.equals(current)) {
                    return BadgeStatus.ofLatest(current);
                } else {
                    return BadgeStatus.ofOutDated(current);
                }

            } else {
                return BadgeStatus.ofError("no latest");
            }
        } else {
            return BadgeStatus.ofError("no current");
        }
    }
}
