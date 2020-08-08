package nl.appsource.badge.expected;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nl.appsource.badge.BadgeException;

import java.io.Serializable;
import java.util.function.Function;

public interface GitHub extends Function<GitHub.GitHubKey, String>  {

    @Getter
    @EqualsAndHashCode
    @RequiredArgsConstructor
    class GitHubKey implements Serializable {
        final String owner;
        final String repo;
        final String branch;
    }

    @Override
    String apply(GitHubKey gitHubKey) throws BadgeException;
}

