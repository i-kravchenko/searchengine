package searchengine.services;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.JSOAPConfig;
import searchengine.model.Page;
import searchengine.repository.PageRepository;

import java.io.IOException;

@Service
public class PageService
{
    @Autowired
    private PageRepository repository;

    @Autowired
    private JSOAPConfig config;

    public Page loadPageContent(Page page) {
        String uri = page.getSite().getUrl() + page.getPath();
        try {
            Document document = Jsoup.connect(uri)
                    .userAgent(config.getUserAgent())
                    .referrer(config.getReferer())
                    .get();
            page.setCode(document.connection().response().statusCode());
            page.setContent(document.toString());
            Page pageFromDb = repository.findBySiteIdAndPath(page.getSite().getId(), page.getPath());
            if(pageFromDb != null) {
                page = pageFromDb;
            }
            return repository.save(page);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
