package query;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import retrieval.Models;
import retrieval.TfIdf;

import java.util.ArrayList;


@Controller
public class QueryController {

    @GetMapping("/")
    public String welcome(Model model) {
        model.addAttribute("query", new query.Querycontainer());
        return "index";
    }

    @PostMapping("/query")
    public String querySubmit(@ModelAttribute("query") Querycontainer query,
                              BindingResult bindingResult, Model model) {
        if (query != null) {
            // TODO add drop down to switch between different retrieval algorithms based upon classes implementing Models
            Models m = new TfIdf();
            ArrayList<Integer> documents = m.retrieve(query.getContent());
            System.out.println("The query was " + query.getContent());
        } else {
            return "index";
        }
        return "result";
    }

}