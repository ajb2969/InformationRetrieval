package query;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class QueryController {

    @GetMapping("/")
    public String welcome() {
        return "index";
    }

    @GetMapping("/error")
    public String error() {
        return "An error has occurred";
    }

}
