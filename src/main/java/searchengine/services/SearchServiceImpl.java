package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import searchengine.dto.TextFragment;
import searchengine.dto.exception.IndexPageException;
import searchengine.dto.statistics.SearchQuery;
import searchengine.dto.statistics.SearchResponse;
import searchengine.dto.statistics.SearchResult;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final LemmaService lemmaService;
    private final PageService pageService;
    @Value("${app.snippet-size}")
    private int snippetSize;

    @Override
    public SearchResponse search(SearchQuery searchQuery) {
        try {
            int siteId = searchQuery.getSite();
            SearchResponse response = new SearchResponse(true, null);
            Set<String> lemmaSet = lemmaService.textToLemmaMap(searchQuery.getQuery()).keySet();
            Pageable pageable = PageRequest.of(searchQuery.getOffset(), searchQuery.getLimit());
            List<Lemma> lemmas;
            if (siteId == 0) {
                lemmas = lemmaService.findLemmas(lemmaSet);
            } else {
                lemmas = lemmaService.findLemmas(siteId, lemmaSet);
            }
            if (lemmas.isEmpty()) {
                return response;
            }
            List<Integer> lemmasId = lemmas
                    .stream()
                    .map(Lemma::getId)
                    .toList();
            org.springframework.data.domain.Page<Page> page = pageService.getPagesByLemmasList(lemmasId, pageable);
            response.setCount(page.getTotalElements());
            List<Page> pages = page.getContent();
            List<SearchResult> data = getSearchResults(lemmaSet, pages);
            response.setData(data);
            log.info("Result of calling the search method: {}", response);
            return response;
        } catch (Exception e) {
            log.error("An error occurred in the search method", e);
            throw new IndexPageException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private List<SearchResult> getSearchResults(Set<String> searchQuerySet, List<Page> pages) {
        List<SearchResult> data = pages
                .stream()
                .map(p -> {
                    Site site = p.getSite();
                    int queryLength = String.join("", searchQuerySet).length();
                    String content = Jsoup.parse(p.getContent()).text().toLowerCase();
                    List<TextFragment> fragments = findFragmentsInText(content, searchQuerySet);
                    fragments.sort((f1, f2) -> {
                        Integer rel1 = queryLength - f1.getFragment().replace("^[а-яё]", "").length();
                        Integer rel2 = queryLength - f2.getFragment().replace("^[а-яё]", "").length();
                        return rel1.compareTo(rel2);
                    });
                    TextFragment fragment = fragments.get(0);
                    int size = snippetSize - fragment.getFragment().length();
                    int start = 0;
                    if (fragment.getStart() >= size / 2) {
                        start = fragment.getStart() - size / 2;
                    }
                    int end = start + snippetSize + 1;
                    String snippet = content.substring(start, end)
                            .replace(fragment.getFragment(), "<b>" + fragment.getFragment() + "</b>");
                    float relevance = lemmaService.getRelevance(p.getId(), searchQuerySet);
                    Element title = pageService.getPageElements(p, "title").first();
                    return new SearchResult(
                            site.getUrl(),
                            site.getName(),
                            p.getPath(),
                            title == null ? "Пустой заголовок" : title.text(),
                            snippet,
                            relevance
                    );
                })
                .collect(Collectors.toList());
        Optional<Double> optional = data.stream().map(SearchResult::getRelevance).max(Double::compareTo);
        if (optional.isPresent()) {
            Double max = optional.get();
            data = data.stream().peek(searchResult -> searchResult.setRelevance(searchResult.getRelevance() / max)).toList();
        }
        return data;
    }

    private List<TextFragment> findFragmentsInText(String text, Collection<String> query) {
        StringBuilder stringBuilder = new StringBuilder();
        LuceneMorphology luceneMorph;
        try {
            luceneMorph = new RussianLuceneMorphology();
        } catch (IOException e) {
            log.error("An error occurred in the SearchServiceImpl:findFragmentsInText method", e);
            throw new IndexPageException(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
        int start = 0;
        List<TextFragment> fragments = new ArrayList<>();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (Character.UnicodeBlock.of(ch).equals(Character.UnicodeBlock.CYRILLIC)) {
                if (stringBuilder.isEmpty()) {
                    start = i;
                }
                stringBuilder.append(ch);
                continue;
            }
            if (stringBuilder.isEmpty()) {
                continue;
            }
            String word = stringBuilder.toString();
            stringBuilder = new StringBuilder();
            String fragment = luceneMorph.getNormalForms(word).get(0);
            if (!query.contains(fragment)) {
                continue;
            }
            TextFragment textFragment = new TextFragment(word, start, i - 1);
            if (!fragments.isEmpty()) {
                int lastIndex = fragments.size() - 1;
                TextFragment lastFragment = fragments.get(lastIndex);
                if (textFragment.getStart() - lastFragment.getEnd() <= 5) {
                    lastFragment.setFragment(text.substring(lastFragment.getStart(), textFragment.getEnd() + 1));
                    lastFragment.setEnd(textFragment.getEnd());
                    fragments.set(lastIndex, lastFragment);
                    continue;
                }
            }
            fragments.add(textFragment);
        }
        return fragments;
    }
}
