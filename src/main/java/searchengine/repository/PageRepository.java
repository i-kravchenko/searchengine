package searchengine.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;

import java.util.Collection;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    Page findBySiteIdAndPath(Integer siteId, String path);

    org.springframework.data.domain.Page<Page> findDistinctByLemmasIdIn(Collection<Integer> lemmasId, Pageable pageable);

    Integer countBySiteId(Integer id);
}