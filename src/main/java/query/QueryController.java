package query;

import com.google.common.collect.Lists;
import indexer.Index;
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
import java.util.*;
import java.util.stream.Collectors;


@Controller
public class QueryController {
    private final String documentsPath = "documents/";
    final File temp = new File(documentsPath + "temp.txt");
    private ArrayList<Similarity> currDocuments = new ArrayList<>();
    private HashSet<Integer> seasons = new HashSet<>();
    private Models m;

    enum Active {
        relevance,
        alphabet,
        reverse,
        seasons,
    }

    ;

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
            if (temp.exists()) {
                temp.delete();
            }
            if (m == null || query.getSelectedModel() != "") {
                m = query.getSelectedModel().toLowerCase().equals("tfidf") ? new TfIdf() : new BM25();
            }
            //TODO add query expansion here
            ArrayList<Similarity> documents =
                    m.retrieve(query.getContent());
            for (Similarity sim : documents) {
                sim.setPreview(getLongestIncreasingSequence(sim.getDocumentLink(), query.getContent()));
            }

            this.currDocuments = documents;
            for(Similarity s: this.currDocuments) {
                seasons.add(s.getSeason());
            }

            //if the first document isn't relevant or similar send back no
            // results available
            model.addAttribute("results", documents);
            model.addAttribute("selected",
                    Active.relevance.toString().toLowerCase());
            model.addAttribute("season", "-1");
            model.addAttribute("seasons", new ArrayList<>(seasons));
            model.addAttribute("query", new Querycontainer());
        } else {
            return "index";
        }
        return "result";
    }

    @GetMapping("/alphabetically")
    public String alphabetically(@RequestParam(required = true) boolean backward, Model model) {
        ArrayList<Similarity> temp = Lists.newArrayList(this.currDocuments);
        if (backward) {
            Collections.sort(temp, (o1, o2) -> o2.getDocument_name().compareTo(o1.getDocument_name()));
        } else {
            Collections.sort(temp, Comparator.comparing(Similarity::getDocument_name));
        }
        model.addAttribute("selected", !backward ?
                Active.alphabet.toString().toLowerCase() :
                Active.reverse.toString().toLowerCase());
        model.addAttribute("query", new Querycontainer());
        model.addAttribute("results", temp);
        model.addAttribute("season", "-1");
        model.addAttribute("seasons", new ArrayList<>(seasons));
        return "result";
    }

    @GetMapping("/relevance")
    public String relevance(Model model) {
        model.addAttribute("results", this.currDocuments);
        model.addAttribute("query", new Querycontainer());
        model.addAttribute("selected",
                Active.relevance.toString().toLowerCase());
        model.addAttribute("season", "-1");
        model.addAttribute("seasons", new ArrayList<>(seasons));
        return "result";
    }

    @GetMapping("/seasons")
    public String seasons(@RequestParam(required = true) String season, Model model) {
        ArrayList<Similarity> temp = Lists.newArrayList(this.currDocuments);
        temp = (ArrayList<Similarity>) temp.stream()
                .filter(e -> e.getSeason() == Integer.parseInt(season))
                .collect(Collectors.toList());
        model.addAttribute("results", temp);
        model.addAttribute("query", new Querycontainer());
        model.addAttribute("season", season);
        model.addAttribute("seasons", new ArrayList<>(seasons));
        model.addAttribute("selected",
                Active.seasons.toString().toLowerCase());
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
            String file =
                    new String(Files.readAllBytes(Paths.get(documentsPath + docName)));
            file = file.replaceAll("<[^>]*>", "");
            // get file tokens
            ArrayList<String> tokens =
                    (ArrayList<String>) Arrays.stream(file.split("[ \n]")).collect(Collectors.toList());
            // get query terms
            String[] terms = query.split(" ");
            HashMap<String, ArrayList<Index.Positions>> termPositions = Index.readQueryPositions((ArrayList<String>)Arrays.stream(query.split(" ")).collect(Collectors.toList()));
            //ArrayList<Index.Positions> positions = Arrays.stream(terms).map(termPositions::get).flatMap(Collection::stream).map(e -> e.getPositions()).flatMap(Collection::stream).
            return tokens.stream().limit(WINDOWSIZE).collect(Collectors.joining(" "));


            //TODO figure out previews
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