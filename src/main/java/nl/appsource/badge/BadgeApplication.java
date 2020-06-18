package nl.appsource.badge;

import lombok.extern.slf4j.Slf4j;
import nl.appsource.badge.actual.Actuator;
import nl.appsource.badge.controller.ActuatorController;
import nl.appsource.badge.controller.ActuatorControllerImpl;
import nl.appsource.badge.controller.BadgeController;
import nl.appsource.badge.controller.BadgeControllerImpl;
import nl.appsource.badge.expected.FixedImpl;
import nl.appsource.badge.expected.GitHubImpl;
import nl.appsource.badge.expected.GitLabImpl;
import nl.appsource.badge.output.ShieldsIo;
import nl.appsource.badge.output.Svg;
import org.springframework.fu.jafu.JafuApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.fu.jafu.Jafu.webApplication;
import static org.springframework.fu.jafu.webmvc.WebMvcServerDsl.webMvc;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@Slf4j
public class BadgeApplication {

    private static final MediaType IMAGE_SVGXML = new MediaType("image", "svg+xml", UTF_8);

    private static final Consumer<HttpHeaders> NOCACHE_HEADERS = (header) -> {
        header.set(HttpHeaders.EXPIRES, "0");
        header.setPragma("no-cache");
        header.setCacheControl("no-cache, no-store, max-age=0, must-revalidate");
    };

    private static final Supplier<ServerResponse.BodyBuilder> NOCACHES = () -> ok().headers(NOCACHE_HEADERS);

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

                                                router

                                                        .GET("/gitlab/sha/{id}/{branch}/{commit_sha}/badge.svg",
                                                                (r) -> NOCACHES.get()
                                                                        .contentType(IMAGE_SVGXML)
                                                                        .body(badgeController.badgeGitLab(r.pathVariable("id"), r.pathVariable("branch"), r.pathVariable("commit_sha")))
                                                        )

                                                        .GET("/gitlab/actuator/{id}/{branch}/badge.svg",
                                                                (r) -> NOCACHES.get()
                                                                        .contentType(IMAGE_SVGXML)
                                                                        .body(badgeController.badgeGitLabActuator(r.pathVariable("id"), r.pathVariable("branch"), r.param("actuator_url").get())))

                                                        .GET("/github/sha/{owner}/{repo}/{branch}/{commit_sha}/badge.svg",
                                                                (r) -> NOCACHES.get()
                                                                        .contentType(IMAGE_SVGXML)
                                                                        .body(badgeController.badgeGitHub(r.pathVariable("owner"), r.pathVariable("repo"), r.pathVariable("branch"), r.pathVariable("commit_sha"))))


                                                        .GET("/github/actuator/{owner}/{repo}/{branch}/badge.svg",
                                                                (r) -> NOCACHES.get()
                                                                        .contentType(IMAGE_SVGXML)
                                                                        .body(badgeController.badgeGitHubActuator(r.pathVariable("owner"), r.pathVariable("repo"), r.pathVariable("branch"), r.param("actuator_url").get())))

                                                        .GET("/github/sha/{owner}/{repo}/{branch}/{commit_sha}",
                                                                (r) -> NOCACHES.get()
                                                                        .contentType(APPLICATION_JSON)
                                                                        .body(badgeController.shieldsIoGitHub(r.pathVariable("owner"), r.pathVariable("repo"), r.pathVariable("branch"), r.pathVariable("commit_sha"))))

                                                        .GET("/github/actuator/{owner}/{repo}/{branch}",
                                                                (r) -> NOCACHES.get()
                                                                        .contentType(APPLICATION_JSON)
                                                                        .body(badgeController.shieldsIoActuator(r.pathVariable("owner"), r.pathVariable("repo"), r.pathVariable("branch"), r.param("actuator_url").get())))

                                                        .GET("/fixed/actuator/{latest}",
                                                                (r) -> NOCACHES.get()
                                                                        .contentType(APPLICATION_JSON)
                                                                        .body(badgeController.shieldsIoActuator(r.pathVariable("latest"), r.param("actuator_url").get())))


                                                        .GET("/actuator/info",
                                                                (r) -> NOCACHES.get()
                                                                        .contentType(APPLICATION_JSON)
                                                                        .body(actuatorController.info()))
                                                        .GET("/actuator/health",
                                                                (r) -> NOCACHES.get()
                                                                        .contentType(APPLICATION_JSON)
                                                                        .body(actuatorController.health()));
                                            }
                                    )
                                    .converters(c -> c
                                            .resource()
                                            .string()
                                            .jackson()
                                    )
                            )
                    )
    );

    public static void main(String[] args) {
        app.run(args);
    }

}
