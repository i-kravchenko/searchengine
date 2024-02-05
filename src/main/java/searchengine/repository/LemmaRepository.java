package searchengine.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;

import java.util.Collection;
import java.util.List;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer>
{
    Lemma findBySiteIdAndLemma(Integer id, String lemma);
    List<Lemma> findAllBySiteId(Integer id);
    List<Lemma> findAllByLemmaInOrderByFrequency(Collection<String> lemmas);
    List<Lemma> findAllBySiteIdAndLemmaInOrderByFrequency(Integer siteId, Collection<String> lemmas);

    @Query("select MAX(frequency) from Lemma")
    Integer getMaxFrequency();

    Integer countBySiteId(Integer id);
}
