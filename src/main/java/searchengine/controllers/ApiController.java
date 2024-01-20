package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexPageService indexPageService;
    private final SearchService searchService;

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        log.info("method /statistic was called");
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<Response> startIndexing() {
        try {
            log.info("method /startIndexing was called");
            boolean result = indexPageService.startIndexing();
            Response response = new Response(result, null);
            log.info("Result of calling the startIndexing method: {}", response);
            return ResponseEntity.ok(response);
        } catch (IndexPageException e) {
            log.error("An error occurred in the startIndexing method", e);
            Response response = new Response(false, e.getMessage());
            return new ResponseEntity<>(response, e.getCode());
        }
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Response> stopIndexing() {
        try {
            log.info("method /stopIndexing was called");
            boolean result = indexPageService.stopIndexing();
            Response response = new Response(result, null);
            log.info("Result of calling the stopIndexing method: {}", response);
            return ResponseEntity.ok(response);
        } catch (IndexPageException e) {
            log.error("An error occurred in the stopIndexing method", e);
            Response response = new Response(false, e.getMessage());
            return new ResponseEntity<>(response, e.getCode());
        }
    }

    @PostMapping("/indexPage")
    public ResponseEntity<Response> indexPage(@RequestParam String url) {
        try {
            log.info("method /indexPage was called");
            boolean result = indexPageService.indexPage(url);
            Response response = new Response(result, null);
            log.info("Result of calling the indexPage method: {}", response);
            return ResponseEntity.ok(response);
        } catch (IndexPageException e) {
            log.error("An error occurred in the stopIndexing method", e);
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
        if(query.isBlank()) {
            Response response = new Response(false, "query is required");
            return ResponseEntity.badRequest().body(response);
        }
        log.info("method /search was called");
        SearchQuery searchQuery = new SearchQuery(query, offset, limit, site);
        return ResponseEntity.ok(searchService.search(searchQuery));
    }
}
