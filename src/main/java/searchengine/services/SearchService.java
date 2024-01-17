package searchengine.services;

import searchengine.dto.statistics.SearchQuery;
import searchengine.dto.statistics.SearchResponse;

public interface SearchService
{
    SearchResponse search(SearchQuery searchQuery);
}
