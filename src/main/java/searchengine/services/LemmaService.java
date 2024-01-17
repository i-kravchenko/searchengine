package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import searchengine.dto.exception.IndexPageException;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
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
                    lemma.setId(lemmaFromDb.getId());
                    lemma.setFrequency(lemmaFromDb.getFrequency() + 1);
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

    public org.springframework.data.domain.Page<Lemma> getLemmasByLemmasList(Set<String> lemmas, Pageable pageable) {
        return repository.findDistinctByLemmaInOrderByFrequency(lemmas, pageable);
    }

    public org.springframework.data.domain.Page<Lemma> getLemmasByLemmasList(int siteId, Set<String> lemmas, Pageable pageable) {
        return repository.findDistinctBySiteIdAndLemmaInOrderByFrequency(siteId, lemmas, pageable);
    }
}
