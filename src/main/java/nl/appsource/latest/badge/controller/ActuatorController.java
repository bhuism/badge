package nl.appsource.latest.badge.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ActuatorController {

    @Value("classpath:/info.json")
    private Resource info;

    @GetMapping(value = "/actuator/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<InputStreamResource> info() throws IOException {
        return ResponseEntity.ok(new InputStreamResource(info.getInputStream()));
    }

    @GetMapping(value = "/actuator/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("{\"status\":\"UP\"}");
    }

}
