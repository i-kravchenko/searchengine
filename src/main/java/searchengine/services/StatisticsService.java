package searchengine.services;

import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.Site;

public interface StatisticsService {
    StatisticsResponse getStatistics();
}
