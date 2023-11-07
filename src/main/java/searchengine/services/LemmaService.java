package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LemmaService {
    @Autowired
    private LemmaRepository repository;

    @Autowired
    private IndexRepository indexRepository;

    public void parsePageContent(Page page) {
        try {
            Map<String, Integer> lemmaMap = textToLemmaToMap(page.getContent());
            List<Lemma> lemmaList = lemmaMap.keySet()
                    .stream()
                    .map(word -> {
                        Lemma lemma = new Lemma();
                        lemma.setLemma(word);
                        lemma.setSite(page.getSite());
                        return lemma;
                    })
                    .toList();
            repository
                    .saveAll(lemmaList)
                    .forEach(lemma -> {
                        lemma = repository.save(lemma);
                        Index index = new Index();
                        index.setPage(page);
                        index.setLemma(lemma);
                        index.setRank(lemmaMap.get(lemma.getLemma()));
                        indexRepository.save(index);
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Integer> textToLemmaToMap(String text) throws IOException {
        HashMap<String, Integer> map = new HashMap<>();
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        text = Jsoup.parse(text).text().toLowerCase().replaceAll("[\\pP]", "");
        Arrays.stream(text.split(" "))
                .filter(luceneMorph::checkString)
                .filter(word -> !word.isEmpty() && !anyWordBaseBelongToParticle(luceneMorph.getMorphInfo(word)))
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

    private boolean anyWordBaseBelongToParticle(List<String> morphInfo) {
        return morphInfo.stream().anyMatch(this::hasParticleProperty);
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
}
