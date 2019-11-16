package query;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class QueryController {

    @GetMapping("/")
    public String welcome(Model model) {
        model.addAttribute("query", new query.Querycontainer());
        return "index";
    }

    @PostMapping("/query")
    public String querySubmit(@ModelAttribute("query") Querycontainer query, BindingResult bindingResult, Model model) {
        if(query != null) {
            System.out.println("The query was " + query.getContent());
        }
        return "result";
    }

}