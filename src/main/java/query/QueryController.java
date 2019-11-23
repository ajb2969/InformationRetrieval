package query;

import com.google.common.collect.ImmutableList;
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
import retrieval.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableList.*;
import static java.util.Collections.*;


@Controller
public class QueryController {
    private final String documentsPath = "documents/";
    final File temp = new File(documentsPath + "temp.txt");
    private ArrayList<Similarity> currDocuments = new ArrayList<>();
    private String currQuery = "";
    private HashSet<Integer> seasons = new HashSet<>();
    private Pooling m;

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

    @ModelAttribute("getSeasons")
    public int[] getSeasonNumbers() {
        return Models.DOCS_TO_SEASONS.values().stream().mapToInt(Integer::intValue).distinct().sorted().toArray();
    }


    @PostMapping("/query")
    public String querySubmit(@ModelAttribute("query") Querycontainer query,
                              Model model) {
        if (query != null) {
            if (temp.exists()) {
                temp.delete();
            }

            m = new Pooling(query.getContent());
            currQuery = query.getContent();
            ArrayList<Similarity> documents = m.retrieve();

            if(query.selectedSeason.size() != 0) {
                documents = (ArrayList<Similarity>) documents.stream().filter(document -> query.selectedSeason.contains(document.getSeason())).collect(Collectors.toList());
                seasons = new HashSet<>(query.getSelectedSeason());
                model.addAttribute("seasons", seasons);
            } else {
                int [] seasonsNumbers = getSeasonNumbers();
                ArrayList<Integer> seasons = new ArrayList<>();
                for(int i = 0; i < seasonsNumbers.length; i++){seasons.add(seasonsNumbers[i]);}
                seasons = new ArrayList<>(seasons);
                model.addAttribute("seasons", seasons);
            }

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
            model.addAttribute("queryContents", currQuery);
            model.addAttribute("season", "-1");
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
            sort(temp, (o1, o2) -> o2.getDocument_name().compareTo(o1.getDocument_name()));
        } else {
            sort(temp, Comparator.comparing(Similarity::getDocument_name));
        }
        model.addAttribute("selected", !backward ?
                Active.alphabet.toString().toLowerCase() :
                Active.reverse.toString().toLowerCase());
        model.addAttribute("query", new Querycontainer());
        model.addAttribute("results", temp);
        model.addAttribute("season", "-1");
        model.addAttribute("queryContents", currQuery);
        model.addAttribute("seasons", seasons);
        return "result";
    }

    @GetMapping("/relevance")
    public String relevance(Model model) {
        model.addAttribute("results", this.currDocuments);
        model.addAttribute("query", new Querycontainer());
        model.addAttribute("selected",
                Active.relevance.toString().toLowerCase());
        model.addAttribute("season", "-1");
        model.addAttribute("queryContents", currQuery);
        model.addAttribute("seasons", seasons);
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
        model.addAttribute("seasons", seasons);
        model.addAttribute("queryContents", currQuery);
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
            final int WINDOWSIZE = 40;
            String file =
                    new String(Files.readAllBytes(Paths.get(documentsPath + docName)));
            file = file.replaceAll("<[^>]*>", "");
            // get file tokens
            ArrayList<String> tokens =
                    (ArrayList<String>) Arrays.stream(file.split("[ \n]")).collect(Collectors.toList());
            // get query terms
            String[] terms = query.split(" ");
            ArrayList<Integer> termPositions = Index.readQueryPositions((ArrayList<String>)Arrays.stream(query.split(" ")).collect(Collectors.toList()), docName);
            int max = 0;
            int maxIndex = 0;
            for(int index = 0; index < termPositions.size(); index++) {
                int temp = index + 1;
                while(temp < termPositions.size() && termPositions.get(temp) < termPositions.get(index)) {
                    termPositions.size();
                    temp += 1;
                }
                if(temp - index > max) {
                    max = temp - index;
                    maxIndex = index;
                }
            }

            String returnedString = "";
            if(termPositions.size() == 0) {
                returnedString = tokens.subList(0, WINDOWSIZE * 2).stream().collect(Collectors.joining(" "));
            } else if(termPositions.size() < 5) {
                returnedString = tokens.subList(termPositions.get(0) - WINDOWSIZE >= 0 ? termPositions.get(0) - WINDOWSIZE: termPositions.get(0),
                        termPositions.get(0) + WINDOWSIZE ).stream()
                        .collect(Collectors.joining(" "));
            } else {
                returnedString = tokens.subList(maxIndex - WINDOWSIZE >= 0 ? maxIndex - WINDOWSIZE : maxIndex, maxIndex + WINDOWSIZE).stream().collect(Collectors.joining(" "));
            }
            return returnedString;

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
        templateResolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        templateResolver.setCheckExistence(true);
        templateResolver.setCacheable(true);
        return templateResolver;
    }
}