package it.aulab.devils_chronicle.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import it.aulab.devils_chronicle.models.Article;
import it.aulab.devils_chronicle.models.Category;
import it.aulab.devils_chronicle.models.User;

public interface ArticleRepository extends ListCrudRepository<Article, Long> {

    List<Article> findByCategory(Category category);

    List<Article> findByUser(User user);

    List<Article> findByIsAcceptedTrue();

    List<Article> findByIsAcceptedFalse();

    List<Article> findByIsAcceptedNull();

    // ricerca per titolo, sottotitolo, autore e categoria
    @Query("SELECT a FROM Article a WHERE " +
            "LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.subtitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.user.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.category.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Article> search(@Param("searchTerm") String searchTerm);

}
