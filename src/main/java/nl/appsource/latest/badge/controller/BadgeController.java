package nl.appsource.latest.badge.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.appsource.latest.badge.model.github.GitHubResponse;
import nl.appsource.latest.badge.model.shieldsio.ShieldsIoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class BadgeController {

    private static final String GIT_BRANCHE_WHERE_HEAD_URL = "https://api.github.com/repos/{owner}/{repo}/commits/{commit_sha}/branches-where-head";

    private final RestTemplate restTemplate;

    @ResponseBody
    @GetMapping(value = "/github/latest/{owner}/{repo}/{branch}/{commit_sha}")
    public ShieldsIoResponse latest(@PathVariable("owner") final String owner, @PathVariable("repo") final String repo, @PathVariable("branch") final String branch, @PathVariable("commit_sha") final String commit_sha) {

        log.info("owner=" + owner + ", repo=" + repo + ", branch=" + branch + ", commit_sha=" + commit_sha);

        final ShieldsIoResponse shieldsIoResponse = new ShieldsIoResponse();

        final HttpHeaders headers = new HttpHeaders();

        headers.setAccept(Collections.singletonList(MediaType.valueOf("application/vnd.github.groot-preview+json")));

        final HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<GitHubResponse[]> gitHubResponseEntity = null;

        try {

            Map<String, String> vars = new HashMap<>();

            vars.put("owner", owner);
            vars.put("repo", repo);
            vars.put("commit_sha", commit_sha);

            gitHubResponseEntity = restTemplate.exchange(GIT_BRANCHE_WHERE_HEAD_URL, HttpMethod.GET, entity, GitHubResponse[].class, vars);

            if (gitHubResponseEntity.getStatusCode().equals(HttpStatus.OK)) {

                final List<GitHubResponse> gitHubResponse = Arrays.asList(gitHubResponseEntity.getBody());

                if (gitHubResponse.stream().anyMatch(g  -> {
                    return g.getName().equals(branch);
                })) {
                    shieldsIoResponse.setMessage("yes");
                    shieldsIoResponse.setColor("green");
                } else {
                    shieldsIoResponse.setMessage("no");
                    shieldsIoResponse.setColor("orange");
                }

            }
        } catch (Exception e) {
            log.info("" + gitHubResponseEntity, e);
            shieldsIoResponse.setMessage(e.getMessage());
            shieldsIoResponse.setColor("red");
            shieldsIoResponse.setCacheSeconds(10L);
        }

        return shieldsIoResponse;
    }

}
