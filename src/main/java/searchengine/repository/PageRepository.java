package searchengine.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;

import java.util.Collection;

@Repository
public interface PageRepository extends PagingAndSortingRepository<Page, Integer> {
    Page findBySiteIdAndPath(Integer siteId, String path);

    @EntityGraph(attributePaths = {"lemmas"})
    org.springframework.data.domain.Page<Page> findAllByLemmasIdIn(Collection<Integer> lemmasId, Pageable pageable);
}