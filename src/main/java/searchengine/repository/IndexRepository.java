package searchengine.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;

@Repository
public interface IndexRepository extends CrudRepository<Index, Integer>
{
    @Query("select SUM(rank) from Index where page.id=:id")
    Float getSumRankByPageIdAndLemmasLemmaIn(Integer id);
}
