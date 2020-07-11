package nl.appsource.badge.controller;

import nl.appsource.badge.BadgeApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ActuatorControllerImpl implements ActuatorController {

    private final String info;

    public ActuatorControllerImpl() throws IOException {
        info = FileCopyUtils.copyToString(new InputStreamReader(new ClassPathResource("/info.json").getInputStream(), UTF_8));
    }

    @Override
    public String info() {
        return info;
    }

    @Override
    public String health() {
        return "{\"status\": \"UP\"}";
    }

    @Override
    public String cache() {
        return "{\"size\": " + BadgeApplication.cache.size() + "}";
    }

}
