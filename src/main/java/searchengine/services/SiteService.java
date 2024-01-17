package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Streamable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import searchengine.dto.config.SitesList;
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
    private final SiteRepository siteRepository;
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
        List<Site> siteList = sites.getSites()
                .stream()
                .peek(site -> site.setStatus(Status.INDEXING))
                .toList();
        return (List<Site>) siteRepository.saveAll(siteList);
    }

    public void stopIndexing() {
        List<Site> sites = getSitesByStatus(Status.INDEXING)
                .stream()
                .peek(site -> {
                    site.setStatus(Status.FAILED);
                    site.setLastError("Индексация остановлена пользователем");
                }).collect(Collectors.toList());
        siteRepository.saveAll(sites);
    }

    public List<Site> getSitesByStatus(Status status) {
        return siteRepository
                .findByStatus(status);
    }

    public void catchException(Site site) {
        site.setStatus(Status.FAILED);
        site.setLastError("Во время индексации произошла ошибка.");
        siteRepository.save(site);
    }

    public void finishIndexing(Site site) {
        boolean isIndexing = siteRepository
                .findByUrl(site.getUrl())
                .getStatus()
                .equals(Status.INDEXED);
        if (isIndexing) {
            site.setStatus(Status.INDEXED);
            siteRepository.save(site);
        }
    }
}
