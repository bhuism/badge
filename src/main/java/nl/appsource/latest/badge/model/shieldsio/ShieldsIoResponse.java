package nl.appsource.latest.badge.model.shieldsio;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShieldsIoResponse {

    private Long schemaVersion = 1L;
    private String label = null;
    private String message = "message";
    private String color = "color";
    private Long cacheSeconds = 10L;

}
