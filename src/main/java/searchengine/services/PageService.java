package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import searchengine.dto.config.JsoupConfig;
import searchengine.dto.exception.IndexPageException;
import searchengine.model.Page;
import searchengine.repository.PageRepository;

import java.io.IOException;
import java.util.Objects;

@Slf4j
@Service
public class PageService
{
    @Autowired
    private PageRepository repository;

    @Autowired
    private JsoupConfig config;

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
                page.setId(pageFromDb.getId());
            }
            return repository.save(page);
        } catch (IOException e) {
            log.error("An error occurred in the PageService:loadPageContent method", e);
            throw new IndexPageException(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    public Elements getPageElements(Page page, String selector) {
        String content = page.getContent();
        return Objects.requireNonNull(Jsoup.parse(content).select(selector));
    }

    public Page findPage(Page page) {
        return repository.findBySiteIdAndPath(page.getSite().getId(), page.getPath());
    }
}
