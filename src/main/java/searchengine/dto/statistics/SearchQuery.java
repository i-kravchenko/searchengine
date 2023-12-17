package searchengine.dto.statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SearchQuery
{
    private String query;
    private int offset;
    private int limit;
    private int site;
}
