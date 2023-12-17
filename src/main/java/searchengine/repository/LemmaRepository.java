package searchengine.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;

import java.util.List;
import java.util.Set;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer>
{
    Lemma findBySiteIdAndLemma(Integer id, String lemma);

    List<Lemma> findAllBySiteId(Integer id);

    Page<Lemma> findDistinctByLemmaInOrderByFrequency(Set<String> lemmas, Pageable pageable);

    Page<Lemma> findDistinctBySiteIdAndLemmaInOrderByFrequency(Integer siteId, Set<String> lemmas, Pageable pageable);
}
