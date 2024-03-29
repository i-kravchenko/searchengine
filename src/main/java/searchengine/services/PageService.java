package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import searchengine.dto.config.JsoupConfig;
import searchengine.dto.exception.IndexPageException;
import searchengine.model.Page;
import searchengine.repository.PageRepository;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PageService
{
    private final PageRepository repository;
    private final JsoupConfig config;

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
            return pageFromDb != null ? pageFromDb : repository.save(page);
        } catch (IOException e) {
            log.error("An error occurred in the PageService:loadPageContent method", e);
            throw new IndexPageException(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    public Elements getPageElements(Page page, String selector) {
        String content = page.getContent();
        return Jsoup.parse(content).select(selector);
    }

    public org.springframework.data.domain.Page<Page> getPagesByLemmasList(List<Integer> lemmas, Pageable pageable) {
        return repository.findDistinctByLemmasIdIn(lemmas, pageable);
    }

    public Integer getCountBySiteId(Integer id) {
        return repository.countBySiteId(id);
    }
}
