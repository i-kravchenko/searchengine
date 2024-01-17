package searchengine.services;

public interface IndexPageService
{
    boolean indexPage(String url);
    boolean startIndexing();
    boolean stopIndexing();
}
