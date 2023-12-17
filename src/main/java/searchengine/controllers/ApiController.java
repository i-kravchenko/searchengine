package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.Response;
import searchengine.dto.exception.IndexPageException;
import searchengine.dto.statistics.SearchQuery;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexPageService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexPageService indexPageService;
    private final SearchService searchService;

    @Autowired
    public ApiController(StatisticsService statisticsService, IndexPageService indexPageService, SearchService searchService) {
        this.statisticsService = statisticsService;
        this.indexPageService = indexPageService;
        this.searchService = searchService;
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
            Response response = new Response(false, e.getMessage());
            return new ResponseEntity<>(response, e.getCode());
        }
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Response> stopIndexing() {
        try {
            boolean result = indexPageService.stopIndexing();
            Response response = new Response(result, null);
            return ResponseEntity.ok(response);
        } catch (IndexPageException e) {
            Response response = new Response(false, e.getMessage());
            return new ResponseEntity<>(response, e.getCode());
        }
    }

    @PostMapping("/indexPage")
    public ResponseEntity<Response> indexPage(@RequestParam String url) {
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
    public ResponseEntity<Response> search(
            @RequestParam String query,
            @RequestParam(required = false, defaultValue = "0") int offset,
            @RequestParam(required = false, defaultValue = "20") int limit,
            @RequestParam(required = false, defaultValue = "0") int site
    ) {
        SearchQuery searchQuery = new SearchQuery(query, offset, limit, site);
        return ResponseEntity.ok(searchService.search(searchQuery));
    }
}
