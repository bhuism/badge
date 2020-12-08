package nl.appsource.badge.controller;

import org.springframework.web.servlet.function.ServerRequest;

import java.util.Map;

public interface ActuatorController {

    String info();

    Map<?, ?> health();

    Map<?, ?> cache();

    Map<?, ?> index(ServerRequest r);

}
