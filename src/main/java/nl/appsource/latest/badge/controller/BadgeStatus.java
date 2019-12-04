package nl.appsource.latest.badge.controller;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
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

}
