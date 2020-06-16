package nl.appsource.latest.badge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.boot.SpringApplication.run;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Slf4j
@SpringBootApplication(proxyBeanMethods = false)
public class BadgeApplication {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebSecurity
    public static class SecurityJavaConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable);
            http.authorizeRequests(authorizeRequests -> authorizeRequests.anyRequest().permitAll());
            http.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(STATELESS));
        }

    }


    public static void main(String[] args) {
        System.out.println(System.getProperties());
        run(BadgeApplication.class, args);
    }

}
