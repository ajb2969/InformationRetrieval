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

import javax.print.DocFlavor;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;


@Controller
public class QueryController {
    private final String documentsPath = "documents/";
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
            //TODO add query expansion here
            ArrayList<TfIdf.Similarity> documents =
                    m.retrieve(query.getContent());
            for (TfIdf.Similarity sim : documents) {
                sim.setPreview(getLongestIncreasingSequence(sim.getDocument_name(), query.getContent()));
            }
            //if the first document isn't relevant or similar send back no results available
            if(documents.get(0).getSimilarity() == 1) {
                model.addAttribute("results", new ArrayList<>());
            } else {
                model.addAttribute("results", documents);
            }
        } else {
            return "index";
        }
        return "result";
    }

    @GetMapping("/document")
    @ResponseBody
    public FileSystemResource getDocument(@RequestParam(required = true) String doc,
                                          Model model) {
        return new FileSystemResource(new File(documentsPath + doc));
    }

    private String getLongestIncreasingSequence(String docName, String query) {
        try {
            String file = new String(Files.readAllBytes(Paths.get(documentsPath + docName)));
            file = file.replaceAll("<[^>]*>", "");
            ArrayList<String> tokens = (ArrayList<String>) Arrays.stream(file.split(" "))
                    .filter(token -> !token.isEmpty() || !token.equals(""))
                    .map(token -> token.replaceAll("[.,!?:\\[\\]]", ""))
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());




        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Document Preview";
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
        ClassLoaderTemplateResolver templateResolver =
                new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/documents/");
        templateResolver.setSuffix(".txt");
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateResolver.setCharacterEncoding("UTF8");
        templateResolver.setCheckExistence(true);
        templateResolver.setCacheable(false);
        return templateResolver;
    }
}