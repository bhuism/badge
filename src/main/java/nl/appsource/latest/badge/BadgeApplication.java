package nl.appsource.latest.badge;

import nl.appsource.latest.badge.filter.Slf4jMDCFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.client.RestTemplate;

@Configuration
@Import({
        DispatcherServletAutoConfiguration.class,
        RestTemplateAutoConfiguration.class,
        ServletWebServerFactoryAutoConfiguration.class,
        WebMvcAutoConfiguration.class,
})
@ComponentScan(basePackageClasses = BadgeApplication.class)
public class BadgeApplication {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Configuration
    @EnableWebSecurity
    public static class SecurityJavaConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable);
            http.authorizeRequests(authorizeRequests -> authorizeRequests.anyRequest().permitAll());
            http.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        }

    }

    @Configuration
    public static class Slf4jMDCFilterConfiguration {

        @Bean
        public FilterRegistrationBean servletRegistrationBean() {
            final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
            final Slf4jMDCFilter log4jMDCFilterFilter = new Slf4jMDCFilter();
            registrationBean.setFilter(log4jMDCFilterFilter);
            registrationBean.setOrder(2);
            return registrationBean;
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(BadgeApplication.class, args);
    }

}
