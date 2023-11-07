package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.SearchQuery;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexPageService
{
    @Autowired
    private PageService pageService;
    @Autowired
    private SiteService siteService;
    @Autowired
    private LemmaService lemmaService;

    public boolean indexPage(String url) {
        Site site = siteService.getSiteByUrl(url);
        Page page = new Page();
        page.setSite(site);
        page.setPath(url.replace(site.getUrl(), ""));
        page = pageService.loadPageContent(page);
        lemmaService.parsePageContent(page);
        return true;
    }

    public boolean startIndexing() {
        return siteService.startIndexing();
    }

    public boolean stopIndexing() {
        return siteService.stopIndexing();
    }

    public ResponseEntity<List<Site>> search(SearchQuery searchQuery) {

        return null;
    }
}
