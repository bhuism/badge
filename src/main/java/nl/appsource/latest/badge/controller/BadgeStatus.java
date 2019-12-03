package nl.appsource.latest.badge.controller;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BadgeStatus {

    ERROR("error", "#555", "red"),
    OUTDATED("outdated", "#555", "orange"),
    LATEST("latest", "#555", "#4c1");

    private final String labelText;
    private final String labelColor;
    private final String messageColor;

}
