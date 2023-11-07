package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import searchengine.config.JSOAPConfig;
import searchengine.config.SitesList;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.task.SiteMap;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class SiteService
{
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private PageRepository pageRepository;
    private final JSOAPConfig config;
    private final SitesList sites;

    public List<Site> getSitesList() {
        return Streamable.of(siteRepository.findAll()).toList();
    }

    public Site getSiteByUrl(String url) {
        Site site = sites.getSites()
                .stream()
                .filter(s -> url.startsWith(s.getUrl()))
                .findFirst().get();
        if(site == null) {
            throw new RuntimeException("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
        }
        Site siteFromDb = siteRepository.findByUrl(site.getUrl());
        if(siteFromDb == null) {
            site.setStatus(Status.INDEXED);
            siteRepository.save(site);
        } else {
            site = siteFromDb;
        }
        return site;
    }

    public boolean startIndexing() {
        siteRepository.deleteAll();
        List<Thread> threads = new ArrayList<>();
        sites.getSites().forEach(site -> threads.add(new Thread(() -> {
            try {
                site.setStatus(Status.INDEXING);
                Page page = new Page();
                page.setSite(siteRepository.save(site));
                page.setPath("/");
                new ForkJoinPool().invoke(new SiteMap(page, config, pageRepository));
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        })));
        threads.forEach(Thread::start);
        return true;
    }

    public boolean stopIndexing() {
        SiteMap.terminate();
        return true;
    }
}
