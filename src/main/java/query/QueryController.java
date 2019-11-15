package query;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

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
