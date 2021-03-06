/**
 * 
 */
package app.controller;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.customsearch.model.Result;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;

import app.api.CustomSearchAPI;
import app.api.YoutubeAPI;
import app.util.Const;
import app.util.ResultListParser;

/**
 * @author Isolachine
 *
 */
@Controller
@RestController
public class ResultController {
    @RequestMapping("index")
    public Model index(Model model) {
        return model;
    }
    
    @RequestMapping("youtube")
    @ResponseBody
    public SearchListResponse youtube(@RequestParam(value = "query", required = true, defaultValue = "") String query) {
        try {
            return new YoutubeAPI().search(query, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping("cse")
    @ResponseBody
    public List<Result> cse(@RequestParam(value = "query", required = true, defaultValue = "") String query) {
        try {
            return new CustomSearchAPI().cse(query);
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping("results")
    public Model results(@RequestParam(value = "query", required = true, defaultValue = "") String query, Model model) throws JsonParseException, JsonMappingException, IOException, GeneralSecurityException {
        List<Result> results;

        if (Const.DEV_ENVIRONMENT) {
            ObjectMapper mapper = new ObjectMapper();
            results = mapper.readValue(new File(Const.TEST_JSON), new TypeReference<List<Result>>() {
            });
        } else {
            results = new ArrayList<>();
            try {
                results = new CustomSearchAPI().cse(query);
                model.addAttribute("results", results);
            } catch (GeneralSecurityException e) {
                model.addAttribute("results", results);
                e.printStackTrace();
            } catch (IOException e) {
                model.addAttribute("results", results);
                e.printStackTrace();
            }
        }

        results = new ResultListParser().parseCSEList(results);

        YoutubeAPI youtubeAPI = new YoutubeAPI();
        for (Result result : results) {
            SearchListResponse response;
            if (result.getPagemap().get("metatags") != null && result.getPagemap().get("metatags").get(0).get("og:type").equals("video.movie")) {
                response = youtubeAPI.search(result.getTitle(), true);
                List<SearchResult> youtubeResults = response.getItems();
                if (youtubeResults.size() > 0) {
                    SearchResult youtubeRes = youtubeResults.get(0);
                    if (youtubeRes.getId().getVideoId() != null) {
                        result.setHtmlFormattedUrl("https://www.youtube.com/watch?v=" + youtubeRes.getId().getVideoId());
                    }
                }
            } else {
                result.setHtmlFormattedUrl("https://www.youtube.com/results?search_query=" + result.getTitle());
            }
        }

        model.addAttribute("query", query);
        model.addAttribute("count", results.size());
        model.addAttribute("results", results);
        return model;
    }

}
