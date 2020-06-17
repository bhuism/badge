package nl.appsource.latest.badge;

import lombok.extern.slf4j.Slf4j;
import nl.appsource.latest.badge.actual.Actuator;
import nl.appsource.latest.badge.controller.ActuatorController;
import nl.appsource.latest.badge.controller.BadgeController;
import nl.appsource.latest.badge.expected.FixedImpl;
import nl.appsource.latest.badge.expected.GitHubImpl;
import nl.appsource.latest.badge.expected.GitLabImpl;
import nl.appsource.latest.badge.output.ShieldsIo;
import nl.appsource.latest.badge.output.Svg;
import org.springframework.fu.jafu.JafuApplication;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.fu.jafu.Jafu.webApplication;
import static org.springframework.fu.jafu.webmvc.WebMvcServerDsl.webMvc;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.servlet.function.ServerResponse.ok;

@Slf4j
public class BadgeApplication {

    private static final MediaType IMAGE_SVGXML = new MediaType("image", "svg+xml", UTF_8);

    public static JafuApplication app = webApplication(
            a -> a.beans(b -> b
                    .bean(RestTemplate.class)
                    .bean(Actuator.class)
                    .bean(GitHubImpl.class)
                    .bean(GitLabImpl.class)
                    .bean(FixedImpl.class)
                    .bean(BadgeController.class)
                    .bean(ActuatorController.class)
                    .bean(Svg.class)
                    .bean(ShieldsIo.class)
            )
                    .enable(webMvc(s -> s
                                    .port(8080)
                                    .router(router -> {
                                                final BadgeController badgeController = s.ref(BadgeController.class);
                                                final ActuatorController actuatorController = s.ref(ActuatorController.class);

                                                router

                                                        .GET("/gitlab/sha/{id}/{branch}/{commit_sha}/badge.svg", (r) ->
                                                                ok()
                                                                        .contentType(IMAGE_SVGXML)
                                                                        .body(badgeController.badgeGitLab(r.pathVariable("id"), r.pathVariable("branch"), r.pathVariable("commit_sha")).getBody()))

                                                        .GET("/gitlab/actuator/{id}/{branch}/badge.svg", (r) ->
                                                                ok()
                                                                        .contentType(IMAGE_SVGXML)
                                                                        .body(badgeController.badgeGitLabActuator(r.pathVariable("id"), r.pathVariable("branch"), r.param("actuator_url").get()).getBody()))

                                                        .GET("/github/sha/{owner}/{repo}/{branch}/{commit_sha}/badge.svg", (r) ->
                                                                ok()
                                                                        .contentType(IMAGE_SVGXML)
                                                                        .body(badgeController.badgeGitHub(r.pathVariable("owner"), r.pathVariable("repo"), r.pathVariable("branch"), r.pathVariable("commit_sha")).getBody()))


                                                        .GET("/github/actuator/{owner}/{repo}/{branch}/badge.svg", (r) ->
                                                                ok()
                                                                        .contentType(IMAGE_SVGXML)
                                                                        .body(badgeController.badgeGitHubActuator(r.pathVariable("owner"), r.pathVariable("repo"), r.pathVariable("branch"), r.param("actuator_url").get()).getBody()))

                                                        .GET("/github/sha/{owner}/{repo}/{branch}/{commit_sha}", (r) ->
                                                                ok()
                                                                        .contentType(APPLICATION_JSON)
                                                                        .body(badgeController.shieldsIoGitHub(r.pathVariable("owner"), r.pathVariable("repo"), r.pathVariable("branch"), r.pathVariable("commit_sha"))))

                                                        .GET("/github/actuator/{owner}/{repo}/{branch}", (r) ->
                                                                ok()
                                                                        .contentType(APPLICATION_JSON)
                                                                        .body(badgeController.shieldsIoActuator(r.pathVariable("owner"), r.pathVariable("repo"), r.pathVariable("branch"), r.param("actuator_url").get())))

                                                        .GET("/fixed/actuator/{latest}", (r) ->
                                                                ok()
                                                                        .contentType(APPLICATION_JSON)
                                                                        .body(badgeController.shieldsIoActuator(r.pathVariable("latest"), r.param("actuator_url").get())))


                                                        .GET("/actuator/info", (request) -> ok().contentType(APPLICATION_JSON).body(actuatorController.info()))
                                                        .GET("/actuator/health", (request) -> ok().contentType(APPLICATION_JSON).body(actuatorController.health()));
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
