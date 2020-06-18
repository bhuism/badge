package nl.appsource.badge.model.actuator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Info {

    @ToString
    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class Git {

        private String branch;

        @ToString
        @Getter
        @Setter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public class Commit {

            private String id;
            private String time;
        }

        private Commit commit;
    }

    private Git git;

}
