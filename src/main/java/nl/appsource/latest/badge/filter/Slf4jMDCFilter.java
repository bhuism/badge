package nl.appsource.latest.badge.filter;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class Slf4jMDCFilter extends OncePerRequestFilter {

    private final static String MDCTOKENKEY = "Slf4jMDCFilter.UUID";

    private final String responseHeader;
    private final String requestHeader;

    public static Supplier<String> TOKEN = () -> MDC.get(MDCTOKENKEY);

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws java.io.IOException, ServletException {
        try {
            final String token;
            if (!StringUtils.isEmpty(requestHeader) && !StringUtils.isEmpty(request.getHeader(requestHeader))) {
                token = request.getHeader(requestHeader);
            } else {
                token = UUID.randomUUID().toString().toUpperCase().replace("-", "");
            }

            Thread.currentThread().setName(token);

            MDC.put(MDCTOKENKEY, token);
            if (!StringUtils.isEmpty(responseHeader)) {
                response.addHeader(responseHeader, token);
            }
            chain.doFilter(request, response);
        } finally {
            MDC.remove(MDCTOKENKEY);
        }
    }

}