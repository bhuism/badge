package nl.appsource.latest.badge.filter;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.function.Supplier;

@Getter
@Setter
public class Slf4jMDCFilter extends OncePerRequestFilter {

    private final static String MDCTOKENKEY = "Slf4jMDCFilter.UUID";

    public static final String RESPONSEHEADERKEY = "X-Badge-Request-Token";

    public static Supplier<String> TOKEN = () -> MDC.get(MDCTOKENKEY);

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws java.io.IOException, ServletException {
        try {

            final String token = UUID.randomUUID().toString().toLowerCase().replace("-", "");

            Thread.currentThread().setName(token);

            MDC.put(MDCTOKENKEY, token);

            if (!StringUtils.isEmpty(RESPONSEHEADERKEY)) {
                response.addHeader(RESPONSEHEADERKEY, token);
            }

            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDCTOKENKEY);
        }
    }

}