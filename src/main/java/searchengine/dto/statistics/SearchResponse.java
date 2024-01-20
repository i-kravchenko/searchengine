package searchengine.dto.statistics;

import lombok.Getter;
import lombok.Setter;
import searchengine.dto.Response;

import java.util.List;

@Getter
@Setter
public class SearchResponse extends Response
{
    private long count = 0;
    private List<SearchResult> data;

    public SearchResponse(boolean result, String error) {
        super(result, error);
    }
}
