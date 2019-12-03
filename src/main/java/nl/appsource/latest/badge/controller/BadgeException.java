package nl.appsource.latest.badge.controller;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BadgeException extends Exception {

    private final BadgeStatus badgeStatus;

}
