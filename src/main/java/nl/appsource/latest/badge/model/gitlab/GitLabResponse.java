package nl.appsource.latest.badge.model.gitlab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitLabResponse {

    private String id;
    private String short_id;

}
