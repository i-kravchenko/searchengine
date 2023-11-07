package searchengine.dto.statistics;

import lombok.Getter;
import lombok.Setter;
import searchengine.model.Site;

@Getter
@Setter
public class SearchQuery
{
    private String query;
    private Site site;
    private int offset;
    private int limit;
}
