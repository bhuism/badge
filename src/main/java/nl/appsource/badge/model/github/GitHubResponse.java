package nl.appsource.badge.model.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubResponse {

    private String name;

    @ToString
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Commit {

        private String sha;
        private String url;

    }

    private Commit commit;

    @JsonProperty("protected")
    private Boolean prootected;

}
