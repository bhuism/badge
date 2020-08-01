package nl.appsource.badge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.badge.actual.Actuator;
import nl.appsource.badge.controller.ActuatorController;
import nl.appsource.badge.controller.ActuatorControllerImpl;
import nl.appsource.badge.controller.BadgeController;
import nl.appsource.badge.controller.BadgeControllerImpl;
import nl.appsource.badge.controller.BadgeStatus;
import nl.appsource.badge.expected.FixedImpl;
import nl.appsource.badge.expected.GitHubImpl;
import nl.appsource.badge.expected.GitLabImpl;
import nl.appsource.badge.expected.MyCache;
import nl.appsource.badge.expected.MyCacheImpl;
import nl.appsource.badge.model.actuator.Info;
import nl.appsource.badge.output.ShieldsIo;
import nl.appsource.badge.output.Svg;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.fu.jafu.JafuApplication;
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
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@Slf4j
public class BadgeApplication {

    private static final MediaType IMAGE_SVGXML = new MediaType("image", "svg+xml", UTF_8);

    public static final MyCache<String, BadgeStatus> cache = new MyCacheImpl<>();

    private static final Consumer<HttpHeaders> NOCACHE_HEADERS = (header) -> {
        header.set(HttpHeaders.EXPIRES, "0");
        header.setPragma("no-cache");
        header.setCacheControl("no-cache, no-store, max-age=0, must-revalidate");
    };

    private static final Supplier<ServerResponse.BodyBuilder> NOCACHES = () -> ok().headers(NOCACHE_HEADERS);

    @RequiredArgsConstructor
    private static class RouterCall {
        public final String pattern;
        public final MediaType contentType;
        public final Function<ServerRequest, Object> handlerFunction;
    }

    public static final JafuApplication app = webApplication(

        a -> a.beans(b -> b
            .bean(RestTemplate.class)
            .bean(Actuator.class)
            .bean(GitHubImpl.class)
            .bean(GitLabImpl.class)
            .bean(FixedImpl.class)
            .bean(BadgeControllerImpl.class)
            .bean(ActuatorControllerImpl.class)
            .bean(Svg.class)
            .bean(ShieldsIo.class)
        )
            .enable(webMvc(s -> s
                .port(8080)
                .router(router -> {

                        final BadgeController badgeController = s.ref(BadgeController.class);
                        final ActuatorController actuatorController = s.ref(ActuatorController.class);

                        asList(
                            new RouterCall("/gitlab/sha/{id}/{branch}/{commit_sha}/badge.svg", IMAGE_SVGXML, (r) -> badgeController.badgeGitLab(r.pathVariable("id"), r.pathVariable("branch"), r.pathVariable("commit_sha"))),
                            new RouterCall("/gitlab/actuator/{id}/{branch}/badge.svg", IMAGE_SVGXML, (r) -> badgeController.badgeGitLabActuator(r.pathVariable("id"), r.pathVariable("branch"), r.param("actuator_url").get())),
                            new RouterCall("/github/sha/{owner}/{repo}/{branch}/{commit_sha}/badge.svg", IMAGE_SVGXML, (r) -> badgeController.badgeGitHub(r.pathVariable("owner"), r.pathVariable("repo"), r.pathVariable("branch"), r.pathVariable("commit_sha"))),
                            new RouterCall("/github/actuator/{owner}/{repo}/{branch}/badge.svg", IMAGE_SVGXML, (r) -> badgeController.badgeGitHubActuator(r.pathVariable("owner"), r.pathVariable("repo"), r.pathVariable("branch"), r.param("actuator_url").get())),
                            new RouterCall("/github/sha/{owner}/{repo}/{branch}/{commit_sha}", APPLICATION_JSON, (r) -> badgeController.shieldsIoGitHub(r.pathVariable("owner"), r.pathVariable("repo"), r.pathVariable("branch"), r.pathVariable("commit_sha"))),
                            new RouterCall("/fixed/actuator/{latest}", APPLICATION_JSON, (r) -> badgeController.shieldsIoActuator(r.pathVariable("owner"), r.pathVariable("repo"), r.pathVariable("branch"), r.param("actuator_url").get())),
                            new RouterCall("/github/actuator/{owner}/{repo}/{branch}", APPLICATION_JSON, (r) -> badgeController.shieldsIoActuator(r.pathVariable("latest"), r.param("actuator_url").get())),
                            new RouterCall("/actuator/info", APPLICATION_JSON, (r) -> actuatorController.info()),
                            new RouterCall("/actuator/health", APPLICATION_JSON, (r) -> actuatorController.health()),
                            new RouterCall("/actuator/cache", APPLICATION_JSON, (r) -> actuatorController.cache())
                        ).forEach(rc -> {
                            router.GET(rc.pattern, (r) -> NOCACHES.get().contentType(rc.contentType).body(rc.handlerFunction.apply(r)));
                            router.HEAD(rc.pattern, (r) -> NOCACHES.get().contentType(rc.contentType).build());
                        });

                    }
                )
                .converters(c -> c
                    .resource()
                    .string()
                    .jackson()
                )

            ))
    );

    public static void main(String[] args) throws IOException {

        System.out.println("result: " + Info.Git.Commit.class.toString());

        final ClassLoader defaultClassLoader = ClassUtils.getDefaultClassLoader();
        final Resource resource = new DefaultResourceLoader(defaultClassLoader).getResource("banner.txt");
        final String banner = StreamUtils.copyToString(resource.getInputStream(), UTF_8);

        System.out.println(banner);

        app.run(args);
    }

}
