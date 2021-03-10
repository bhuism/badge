package nl.appsource.badge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.badge.actual.Actuator;
import nl.appsource.badge.actual.MetaTag;
import nl.appsource.badge.cache.MyCache;
import nl.appsource.badge.cache.MyCacheImpl;
import nl.appsource.badge.controller.ActuatorController;
import nl.appsource.badge.controller.ActuatorControllerImpl;
import nl.appsource.badge.expected.GitHub;
import nl.appsource.badge.expected.GitHubImpl;
import nl.appsource.badge.expected.GitLab;
import nl.appsource.badge.expected.GitLabImpl;
import nl.appsource.badge.output.ShieldsIo;
import nl.appsource.badge.output.Svg;
import nl.appsource.badge.service.BadgeController;
import nl.appsource.badge.service.BadgeControllerImpl;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.ClassUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.springframework.fu.jafu.Jafu.webApplication;
import static org.springframework.fu.jafu.webmvc.WebMvcServerDsl.webMvc;
import static org.springframework.http.HttpHeaders.EXPIRES;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@Slf4j
@SpringBootApplication
public class BadgeApplication {

    private static final String VERSION_HEADER = "X-Badge-Version";

    public static final String ACTUATOR = "/actuator";

    private static final MediaType IMAGE_SVGXML = new MediaType("image", "svg+xml", UTF_8);

    private static String commitSha;

    public static final MyCache<String> cache = new MyCacheImpl<>();

    private static final Consumer<HttpHeaders> NOCACHE_HEADERS = (header) -> {
        header.set(EXPIRES, "0");
        header.setPragma("no-cache");
        header.setCacheControl("no-cache, no-store, max-age=0, must-revalidate");
        header.set(VERSION_HEADER, commitSha);
    };

    private static final Supplier<ServerResponse.BodyBuilder> OK_NOCACHE = () -> ok().headers(NOCACHE_HEADERS);

    @RequiredArgsConstructor
    private static class RouterCall {
        public final String pattern;
        public final MediaType contentType;
        public final Function<ServerRequest, Object> handlerFunction;
    }

    public static void main(final String[] args) throws IOException {

        final RestTemplate restTemplate = new RestTemplate();

        final ClassLoader defaultClassLoader = ClassUtils.getDefaultClassLoader();
        final DefaultResourceLoader defaultResourceLoader = new DefaultResourceLoader(defaultClassLoader);
        commitSha = StreamUtils.copyToString(defaultResourceLoader.getResource("version.properties").getInputStream(), UTF_8);

        webApplication(

            a -> a
                .beans(b -> b
                    .bean(RestTemplate.class, () -> restTemplate)
                    .bean(Actuator.class, () -> new Actuator(restTemplate))
                    .bean(GitHubImpl.class, () -> new GitHubImpl(restTemplate))
                    .bean(GitLabImpl.class, () -> new GitLabImpl(restTemplate))
                    .bean(Svg.class, Svg::new)
                    .bean(ActuatorController.class, ActuatorControllerImpl::new)
                    .bean(ShieldsIo.class, ShieldsIo::new)
                    .bean(MetaTag.class, MetaTag::new)
                    .bean(BadgeController.class, () -> new BadgeControllerImpl(b.ref(GitHub.class), b.ref(GitLab.class), b.ref(Actuator.class), b.ref(Svg.class), b.ref(ShieldsIo.class), b.ref(MetaTag.class)))
                )
                .enable(webMvc(s -> s
                    .port(8080)
                    .router(router -> {

                            final BadgeController badgeController = s.ref(BadgeController.class);
                            final ActuatorController actuatorController = s.ref(ActuatorController.class);

                            router.GET("/", (r) ->
                                ok().contentType(MediaType.TEXT_HTML).headers(NOCACHE_HEADERS).body(new ClassPathResource("/static/index.html"))
                            );

                            asList(

                                new RouterCall("/gitlab/sha/{id}/{branch}/{commit_sha}/badge.svg", IMAGE_SVGXML, (r) -> badgeController.badgeGitLab(r.pathVariable("id"), r.pathVariable("branch"), r.pathVariable("commit_sha"))),
                                new RouterCall("/gitlab/actuator/{id}/{branch}/badge.svg", IMAGE_SVGXML, (r) -> badgeController.badgeGitLabActuator(r.pathVariable("id"), r.pathVariable("branch"), r.param("actuator_url").get())),
                                new RouterCall("/gitlab/sha/{id}/{branch}/{commit_sha}", IMAGE_SVGXML, (r) -> badgeController.shieldsIoGitLab(r.pathVariable("id"), r.pathVariable("branch"), r.pathVariable("commit_sha"))),
                                new RouterCall("/gitlab/actuator/{id}/{branch}", IMAGE_SVGXML, (r) -> badgeController.shieldsIoGitLabActuator(r.pathVariable("id"), r.pathVariable("branch"), r.param("actuator_url").get())),

                                new RouterCall("/github/sha/{owner}/{repo}/{branch}/{commit_sha}/badge.svg", IMAGE_SVGXML, (r) -> badgeController.badgeGitHub(r.pathVariable("owner"), r.pathVariable("repo"), r.pathVariable("branch"), r.pathVariable("commit_sha"))),
                                new RouterCall("/github/actuator/{owner}/{repo}/{branch}/badge.svg", IMAGE_SVGXML, (r) -> badgeController.badgeGitHubActuator(r.pathVariable("owner"), r.pathVariable("repo"), r.pathVariable("branch"), r.param("actuator_url").get())),
                                new RouterCall("/github/sha/{owner}/{repo}/{branch}/{commit_sha}", APPLICATION_JSON, (r) -> badgeController.shieldsIoGitHub(r.pathVariable("owner"), r.pathVariable("repo"), r.pathVariable("branch"), r.pathVariable("commit_sha"))),
                                new RouterCall("/github/actuator/{owner}/{repo}/{branch}", APPLICATION_JSON, (r) -> badgeController.shieldsIoGitHubActuator(r.pathVariable("owner"), r.pathVariable("repo"), r.pathVariable("branch"), r.param("actuator_url").get())),

                                new RouterCall("/github/metatag/{owner}/{repo}/{branch}/badge.svg", IMAGE_SVGXML, (r) -> badgeController.badgeGitHubHtmlUrl(r.pathVariable("owner"), r.pathVariable("repo"), r.pathVariable("branch"), r.param("html_url").get())),

                                new RouterCall("/fixed/actuator/{latest}", APPLICATION_JSON, (r) -> badgeController.shieldsIoActuator(r.pathVariable("latest"), r.param("actuator_url").get())),

                                new RouterCall(ACTUATOR, APPLICATION_JSON, (r) -> actuatorController.index(r)),
                                new RouterCall(ACTUATOR + "/info", APPLICATION_JSON, (r) -> actuatorController.info()),
                                new RouterCall(ACTUATOR + "/health", APPLICATION_JSON, (r) -> actuatorController.health()),
                                new RouterCall(ACTUATOR + "/stats", APPLICATION_JSON, (r) -> actuatorController.cache())

                            ).forEach(rc -> {
                                router.GET(rc.pattern, (r) -> OK_NOCACHE.get().contentType(rc.contentType).body(rc.handlerFunction.apply(r)));
                                router.HEAD(rc.pattern, (r) -> OK_NOCACHE.get().contentType(rc.contentType).build());
                            });

                        }
                    )
                    .converters(c -> c
                        .resource()
                        .string()
                        .jackson()
                    )

                ))
        ).run(args);

    }

}
