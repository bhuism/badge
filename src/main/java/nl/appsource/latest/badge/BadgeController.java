package nl.appsource.latest.badge;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class BadgeController {

    @ResponseBody
    @GetMapping(value = "/")
    public BadgeResponse latest() {
        return new BadgeResponse();
    }

}
