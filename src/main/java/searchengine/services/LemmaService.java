package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import searchengine.dto.exception.IndexPageException;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LemmaService {
    private final LemmaRepository repository;
    private final IndexRepository indexRepository;

    public synchronized void parsePageContent(Page page) {
        try {
            Map<String, Integer> lemmaMap = textToLemmaMap(page.getContent());
            List<Lemma> lemmaList = lemmaMap.keySet().stream().map(word -> {
                Lemma lemma = new Lemma();
                lemma.setLemma(word);
                lemma.setFrequency(1);
                lemma.setSite(page.getSite());
                Lemma lemmaFromDb = repository.findBySiteIdAndLemma(lemma.getSite().getId(), lemma.getLemma());
                if(lemmaFromDb != null) {
                    lemmaFromDb.setFrequency(lemmaFromDb.getFrequency() + 1);
                    return lemmaFromDb;
                }
                return lemma;
            }).toList();
            List<Index> indices = new ArrayList<>();
            repository.saveAll(lemmaList).forEach(lemma -> {
                Index index = new Index();
                index.setPage(page);
                index.setLemma(lemma);
                index.setRank(lemmaMap.get(lemma.getLemma()));
                indices.add(index);
            });
            indexRepository.saveAll(indices);
        } catch (IOException e) {
            log.error("An error occurred in the LemmaService:parsePageContent method", e);
            throw new IndexPageException(e.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    public Map<String, Integer> textToLemmaMap(String text) throws IOException {
        HashMap<String, Integer> map = new HashMap<>();
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        text = Jsoup.parse(text).text().toLowerCase().replaceAll("\\pP", "");
        Arrays.stream(text.split(" "))
                .filter(luceneMorph::checkString)
                .filter(word -> !word.isEmpty() && luceneMorph.getMorphInfo(word).stream().noneMatch(this::hasParticleProperty))
                .map(word -> luceneMorph.getNormalForms(word).get(0))
                .forEach(word -> {
                    if (map.containsKey(word)) {
                        map.put(word, map.get(word) + 1);
                    } else {
                        map.put(word, 1);
                    }
                });
        return map;
    }

    private boolean hasParticleProperty(String wordBase) {
        String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};
        for (String property : particlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }

    public void computeFrequency(Site site) {
        List<Lemma> lemmaList = repository.findAllBySiteId(site.getId())
                .stream()
                .peek(lemma -> {
                    lemma.setFrequency(lemma.getPages().size());
                }).toList();
        repository.saveAll(lemmaList);
    }

    private Integer getFrequencyMaxValue() {
        Integer maxFrequency = repository.getMaxFrequency();
        return (int) (maxFrequency * 0.8);
    }

    public List<Lemma> findLemmas(Set<String> lemmas) {
        return repository.findAllByLemmaInOrderByFrequency(lemmas);
    }

    public List<Lemma> findLemmas(Integer siteId, Set<String> lemmas) {
        return repository.findAllBySiteIdAndLemmaInOrderByFrequency(siteId, lemmas);
    }

    public Integer getCountBySiteId(Integer id) {
        return repository.countBySiteId(id);
    }

    public Float getRelevance(Integer id, Set<String> searchQuerySet) {
        return indexRepository.getSumRankByPageIdAndLemmasLemmaIn(id);
    }
}
