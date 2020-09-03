package nl.appsource.badge.controller;

import lombok.SneakyThrows;
import nl.appsource.badge.BadgeApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ActuatorControllerImpl implements ActuatorController {

    private final String info;

    @SneakyThrows
    public ActuatorControllerImpl() {
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
    public Map<?, ?> cache() {
        return Map.of("cache", BadgeApplication.cache.getStats());
    }

}
