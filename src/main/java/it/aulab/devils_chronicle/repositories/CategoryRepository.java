package it.aulab.devils_chronicle.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import it.aulab.devils_chronicle.models.Category;

public interface CategoryRepository extends ListCrudRepository<Category, Long> {

    // Number of articles per category
    @Query("SELECT c.id, COUNT(a) FROM Category c LEFT JOIN c.articles a GROUP BY c.id")
    List<Object[]> findCategoryArticleCounts();

}
