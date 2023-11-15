package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.exception.IndexPageException;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repository.SiteRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SiteService {
    @Autowired
    private SiteRepository siteRepository;
    private final SitesList sites;

    public List<Site> getSitesList() {
        return Streamable.of(siteRepository.findAll()).toList();
    }

    public Site getSiteByUrl(String url) {
        Optional<Site> optional = sites.getSites()
                .stream()
                .filter(s -> url.startsWith(s.getUrl()))
                .findFirst();
        if (optional.isEmpty()) {
            throw new IndexPageException("Данная страница находится за пределами сайтов, указанных в конфигурационном файле", HttpStatus.NOT_FOUND);
        }
        Site site = optional.get();
        Site siteFromDb = siteRepository.findByUrl(site.getUrl());
        if (siteFromDb == null) {
            site.setStatus(Status.INDEXED);
            return siteRepository.save(site);
        } else {
            return siteFromDb;
        }
    }

    public List<Site> startIndexing() {
        siteRepository.deleteAll();
        return sites.getSites()
                .stream()
                .map(site -> {
                    site.setStatus(Status.INDEXING);
                    return siteRepository.save(site);
                }).collect(Collectors.toList());
    }

    public void stopIndexing() {
        List<Site> sites = siteRepository
                .findByStatus(Status.INDEXING);
        sites.forEach(site -> {
            site.setStatus(Status.FAILED);
            site.setLastError("Индексация остановлена пользователем");
        });
        siteRepository.saveAll(sites);
    }

    public void catchExeption(Site site, Exception e) {
        site.setStatus(Status.FAILED);
        site.setLastError(e.getMessage());
        siteRepository.save(site);
    }

    public void finishIndexing(Site site) {
        site.setStatus(Status.INDEXED);
        siteRepository.save(site);
    }
}
