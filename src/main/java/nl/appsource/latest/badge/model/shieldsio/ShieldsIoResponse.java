package nl.appsource.latest.badge.model.shieldsio;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@JsonInclude(NON_NULL)
public class ShieldsIoResponse {

    private Long schemaVersion = 1L;
    private String label = null;
    private String message = "message";
    private String color = "color";
    private Long cacheSeconds = 10L;

}
