package searchengine.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;

@Repository
public interface PageRepository extends PagingAndSortingRepository<Page, Integer> {
    Page findBySiteIdAndPath(Integer siteId, String path);
}