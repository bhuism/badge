package nl.appsource.badge.controller;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.badge.BadgeApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.function.ServerRequest;

import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static java.io.File.separator;
import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
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
    public Map<?, ?> health() {
        return Map.of("status", "UP");
    }

    @Override
    public Map<?, ?> cache() {
        return Map.of("cache", BadgeApplication.cache.getStats());
    }

    @Override
    public Map<?, ?> index(final ServerRequest serverRequest) {

        final String scheme = "https://";
        final String host = serverRequest.servletRequest().getHeader("host");
        final String actuator = scheme + host + BadgeApplication.ACTUATOR;

        final Map maps = new TreeMap<>(Map.of("_self", Map.of("href", actuator)));

        Set.of("info", "health", "stats").forEach(key -> {
            maps.put(key, Map.of("href", actuator + separator + key));
        });

        return Map.of("_links", maps);
    }


}
