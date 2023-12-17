package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import searchengine.dto.exception.IndexPageException;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndexPageService {
    @Autowired
    private PageService pageService;
    @Autowired
    private SiteService siteService;
    @Autowired
    private LemmaService lemmaService;

    class SiteMap extends RecursiveTask<Set<Page>> {
        private Page page;
        private static boolean indexIsStarted = false;

        SiteMap(Page page) {
            this.page = page;
        }

        @SneakyThrows
        @Override
        public Set<Page> compute() {
            if (!indexIsStarted) {
                return new TreeSet<>();
            }
            List<SiteMap> tasks = new ArrayList<>();
            Thread.sleep(1_000);
            page = pageService.loadPageContent(page);
            lemmaService.parsePageContent(page);
            Set<Page> siteMap = Jsoup.parse(page.getContent())
                    .select("a")
                    .stream()
                    .map(link -> formatUri(link.attr("href")))
                    .filter(page -> !page.getPath().equals(this.page.getPath()))
                    .filter(page -> pageService.findPage(page) == null)
                    .collect(Collectors.toSet());
            for (Page page : siteMap) {
                SiteMap map = new SiteMap(page);
                map.fork();
                tasks.add(map);
            }
            for (SiteMap task : tasks) {
                siteMap.addAll(task.join());
            }
            return siteMap;
        }

        private Page formatUri(String path) {
            Page newPage = new Page();
            try {
                newPage.setSite(page.getSite());
                newPage.setPath(page.getPath());
                String address = page.getSite().getUrl();
                if (path.startsWith(address)) {
                    newPage.setPath(new URI(path).getPath());
                } else if (path.startsWith("/")) {
                    newPage.setPath(new URI(address + path).getPath());
                } else if (path.matches("[a-z-_]+")) {
                    newPage.setPath(new URI(path).getPath());
                }
            } catch (URISyntaxException ignored) {
            }
            return newPage;
        }
    }

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
        if (SiteMap.indexIsStarted) {
            throw new IndexPageException("Индексация уже запущена", HttpStatus.BAD_REQUEST);
        }
        SiteMap.indexIsStarted = true;
        Thread thread = new Thread(() -> {
            List<Thread> threads = new ArrayList<>();
            siteService
                    .startIndexing()
                    .forEach(site -> threads.add(new Thread(() -> {
                        try {
                            Page page = new Page();
                            page.setSite(site);
                            page.setPath("/");
                            new ForkJoinPool().invoke(new SiteMap(page));
                            lemmaService.computeFrequency(site);
                            siteService.finishIndexing(site);
                        } catch (Exception e) {
                            lemmaService.computeFrequency(site);
                            siteService.catchException(site, e);
                            if (siteService.getSitesByStatus(Status.INDEXING).isEmpty()) {
                                SiteMap.indexIsStarted = false;
                            }
                        }
                    })));
            threads.forEach(Thread::start);
        });
        thread.start();
        return true;
    }

    public boolean stopIndexing() {
        if (!SiteMap.indexIsStarted) {
            throw new IndexPageException("Индексация не запущена", HttpStatus.BAD_REQUEST);
        }
        siteService.stopIndexing();
        SiteMap.indexIsStarted = false;
        return true;
    }
}
