package nl.appsource.latest.badge.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class ActuatorController {

    private Resource info = new ClassPathResource("/info.json");

    public Resource info() {
        return info;
    }

    public String health() {
        return "{\"status\":\"UP\"}";
    }

}
