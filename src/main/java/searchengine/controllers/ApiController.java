package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    public ResponseEntity<Boolean> startIndexing() {
        boolean result = indexPageService.startIndexing();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<Boolean> stopIndexing() {
        boolean result = indexPageService.stopIndexing();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/indexPage")
    public ResponseEntity<Boolean> indexPage(@RequestParam String url)
    {
        boolean result = indexPageService.indexPage(url);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<Site>> search(@RequestBody SearchQuery searchQuery) {
        return indexPageService.search(searchQuery);
    }
}
