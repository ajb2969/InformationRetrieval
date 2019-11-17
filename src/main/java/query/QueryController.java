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
import retrieval.Similarity;
import retrieval.TfIdf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@Controller
public class QueryController {
    private final String documentsPath = "documents/";
    final File temp = new File(documentsPath + "temp.txt");

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
            if(temp.exists()) {
                temp.delete();
            }
            Models m =
                    query.getSelectedModel().toLowerCase().equals("tfidf") ?
                            new TfIdf() : new BM25();
            //TODO add query expansion here
            ArrayList<Similarity> documents =
                    m.retrieve(query.getContent());
            for ( Similarity sim : documents) {
                sim.setPreview(getLongestIncreasingSequence(sim.getDocument_name(), query.getContent()));
            }
            //if the first document isn't relevant or similar send back no
            // results available
            if (documents.get(0).getSimilarity() == 1) {
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
    public FileSystemResource getDocument(@RequestParam(required = true) String doc, Model model) {
        try {
            if (temp.exists()) {
                temp.delete();
            }
            List<String> selected =
                    Files.readAllLines(Paths.get(documentsPath + doc));
            selected =
                    selected.stream().map(e -> e + "<br>").collect(Collectors.toList());
            Files.write(Paths.get(String.valueOf(temp)), selected);
            return new FileSystemResource(documentsPath + "temp.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new FileSystemResource(documentsPath + doc);
    }

    private String getLongestIncreasingSequence(String docName, String query) {
        try {
            final int WINDOWSIZE = 20;
            int currStart = 0;
            String file =
                    new String(Files.readAllBytes(Paths.get(documentsPath + docName)));
            file = file.replaceAll("<[^>]*>", "");
            // get file tokens
            ArrayList<String> tokens =
                    (ArrayList<String>) Arrays.stream(file.split(" ")).collect(Collectors.toList());
            // get query terms
            String[] terms = query.split(" ");

            int maxTerms = 0;
            String[] bestWindow = new String[0];
            do {
                //populate window
                String[] window = IntStream.range(currStart,
                        currStart + 20 < tokens.size() ?
                                currStart + WINDOWSIZE :
                                tokens.size() - currStart).mapToObj(tokens::get).map(String::toLowerCase).filter(token -> !token.isEmpty() || !token.equals(""))
                        .map(token -> token.replaceAll("[.,!?:\\[\\]]", "")).collect(Collectors.toList()).toArray(new String[WINDOWSIZE]);
                if (maxTerms == terms.length) {
                    bestWindow = window;
                    break;
                } else {
                    int contains = 0;
                    for (String term : terms) {
                        if (Arrays.asList(window).contains(term.toLowerCase())) {
                            contains += 1;
                        }
                    }
                    if (contains > maxTerms) {
                        maxTerms = contains;
                        bestWindow = window;
                    }
                    currStart += 1;
                }
            } while (currStart + WINDOWSIZE < tokens.size());
            if (maxTerms == 0) {
                return tokens.stream().limit(WINDOWSIZE).collect(Collectors.joining(" "));
            }

            return Arrays.stream(bestWindow).map(Object::toString)
                    .collect(Collectors.joining(" "));

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