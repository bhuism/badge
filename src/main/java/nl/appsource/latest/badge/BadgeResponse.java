package nl.appsource.latest.badge;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BadgeResponse {

    private Long schemaVersion = 1L;
    private String label = "label";
    private String message = "message";
    private String color = "blue";
    private Long cacheSeconds = 60L;

}
