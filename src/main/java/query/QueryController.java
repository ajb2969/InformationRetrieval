package query;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import retrieval.BM25;
import retrieval.Models;
import retrieval.TfIdf;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;


@Controller
public class QueryController {

    @GetMapping("/")
    public String welcome(Model model) {
        model.addAttribute("query", new query.Querycontainer());
        return "index";
    }

    @ModelAttribute("getModels")
    public String[] getExtendedModels() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider provider =
                new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(Models.class));
        ArrayList<String> possibleModels = new ArrayList<>();
        Set<BeanDefinition> components = provider.findCandidateComponents(
                "retrieval");
        for (BeanDefinition component : components) {
            Class cls = Class.forName(component.getBeanClassName());
            possibleModels.add(cls.getName().substring(cls.getName().indexOf(
                    ".") + 1));
            // use class cls found
        }
        return possibleModels.toArray(new String[possibleModels.size()]);
    }


    @PostMapping("/query")
    public String querySubmit(@ModelAttribute("query") Querycontainer query,
                              Model model) {
        if (query != null) {
            Models m =
                    query.getSelectedModel().toLowerCase().equals("tfidf") ?
                            new TfIdf() : new BM25();
            ArrayList<String> documents = m.retrieve(query.getContent());
            model.addAttribute("results", documents);
        } else {
            return "index";
        }
        return "result";
    }

    @GetMapping("/document")
    @ResponseBody
    public FileSystemResource getDocument(@RequestParam(required = true) String doc,
                                          Model model) {
        return new FileSystemResource(new File("documents/" + doc));
    }

}



@Configuration
class ThymeleafConfig {

    @Bean(name = "textTemplateEngine")
    public TemplateEngine textTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.addTemplateResolver(textTemplateResolver());
        return templateEngine;
    }

    private ITemplateResolver textTemplateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/documents/");
        templateResolver.setSuffix(".txt");
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCharacterEncoding("UTF8");
        templateResolver.setCheckExistence(true);
        templateResolver.setCacheable(false);
        return templateResolver;
    }
}