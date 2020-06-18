package nl.appsource.badge;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = BadgeApplication.class)
public class BadgeApplicationTests {

    @Test
    void contextLoads() {
        log.info("contextLoads()");
    }

}
