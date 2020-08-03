package nl.appsource.badge.controller;

import java.util.Map;

public interface ActuatorController {

    String info();

    String health();

    Map<?, ?> cache();

}
