package searchengine.task;

import org.hibernate.exception.ConstraintViolationException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import searchengine.config.JSOAPConfig;
import searchengine.model.Page;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

public class SiteMap extends RecursiveTask<Set<Page>> {
    private final Page page;
    static List<SiteMap> tasks = new ArrayList<>();
    @Autowired
    private CrudRepository repository;
    private final JSOAPConfig config;

    public SiteMap(Page page, JSOAPConfig config, CrudRepository repository) throws URISyntaxException {
        this.page = page;
        this.config = config;
        this.repository = repository;
    }

    public static void terminate() {
        tasks.forEach(task -> {
            boolean cancelled = task.isCancelled();
            System.out.println(cancelled);
        });
        tasks.forEach(task -> task.cancel(true));
    }

    @Override
    public Set<Page> compute() {
        try {
            if(isCancelled()) {
                return new TreeSet<>();
            }
            Set<Page> siteMap = parsePage();
            for (Page page : siteMap) {
                SiteMap map = new SiteMap(page, config, repository);
                map.fork();
                tasks.add(map);
            }
            for (SiteMap task : tasks) {
                siteMap.addAll(task.join());
            }
            return siteMap;
        } catch (URISyntaxException e) {
            return new TreeSet<>();
        }
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

    private Set<Page> parsePage() {
        String uri = page.getSite().getUrl() + page.getPath();
        try {
            Thread.sleep(500);
            Document document = Jsoup.connect(uri)
                    .userAgent(config.getUserAgent())
                    .referrer(config.getReferer())
                    .get();
            page.setCode(document.connection().response().statusCode());
            page.setContent(document.toString());
            try {
                repository.save(page);
            } catch (ConstraintViolationException e) {
            }
            return document.select("a")
                    .stream()
                    .map(link -> formatUri(link.attr("href")))
                    .filter(page -> !page.getPath().equals(uri))
                    .collect(Collectors.toSet());
        } catch (InterruptedException | IOException e) {
            return new TreeSet<>();
        }
    }
}
