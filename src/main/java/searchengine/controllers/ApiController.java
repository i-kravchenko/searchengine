package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.Response;
import searchengine.dto.exception.IndexPageException;
import searchengine.dto.statistics.SearchQuery;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.Site;
import searchengine.services.IndexPageService;
import searchengine.services.StatisticsService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexPageService indexPageService;

    @Autowired
    public ApiController(StatisticsService statisticsService, IndexPageService indexPageService) {
        this.statisticsService = statisticsService;
        this.indexPageService = indexPageService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<Response> startIndexing() {
        try {
            boolean result = indexPageService.startIndexing();
            Response response = new Response(result, null);
            return ResponseEntity.ok(response);
        } catch (IndexPageException e) {
            Response response =  new Response(false, e.getMessage());
            return new ResponseEntity<>(response, e.getCode());
        }
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Response> stopIndexing()
    {
        try {
            boolean result = indexPageService.stopIndexing();
            Response response =  new Response(result, null);
            return ResponseEntity.ok(response);
        } catch (IndexPageException e) {
            Response response =  new Response(false, e.getMessage());
            return new ResponseEntity<>(response, e.getCode());
        }
    }

    @PostMapping("/indexPage")
    public ResponseEntity<Response> indexPage(@RequestParam String url)
    {
        try {
            boolean result = indexPageService.indexPage(url);
            Response response = new Response(result, null);
            return ResponseEntity.ok(response);
        } catch (IndexPageException e) {
            Response response = new Response(false, e.getMessage());
            return new ResponseEntity<>(response, e.getCode());
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Site>> search(@RequestBody SearchQuery searchQuery) {
        return indexPageService.search(searchQuery);
    }
}
