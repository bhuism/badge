package nl.appsource.badge.expected;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.appsource.badge.controller.BadgeException;
import nl.appsource.badge.controller.BadgeStatus;

import java.io.Serializable;
import java.util.function.Function;

public interface GitLab extends Function<GitLab.GitLabKey, String> {

    @Getter
    @EqualsAndHashCode
    @RequiredArgsConstructor
    class GitLabKey implements Serializable {
        final String id;
        final String branch;
    }

    @Override
    String apply(final GitLabKey key) throws BadgeException;

}
