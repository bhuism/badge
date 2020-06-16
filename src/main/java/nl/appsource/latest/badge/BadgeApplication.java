package nl.appsource.latest.badge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import static org.springframework.boot.SpringApplication.run;

@Slf4j
@SpringBootApplication(proxyBeanMethods = false)
public class BadgeApplication {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        run(BadgeApplication.class, args);
    }

}
