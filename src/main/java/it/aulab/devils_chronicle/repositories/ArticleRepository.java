package it.aulab.devils_chronicle.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

import it.aulab.devils_chronicle.models.Article;
import it.aulab.devils_chronicle.models.Category;
import it.aulab.devils_chronicle.models.User;

public interface ArticleRepository extends ListCrudRepository<Article, Long> {

    // Trova articoli per categoria
    List<Article> findByCategory(Category category);

    // Trova articoli per autore
    List<Article> findByUser(User user);

    // Trova articoli accettati
    List<Article> findByIsAcceptedTrue();

    // Trova articoli rifiutati
    List<Article> findByIsAcceptedFalse();

    // Trova articoli in attesa di revisione
    List<Article> findByIsAcceptedNull();

    // Trova articoli in evidenza accettati
    List<Article> findByIsFeaturedTrueAndIsAcceptedTrueOrderByPublishDateDesc();

    // Trova articoli in evidenza accettati
    List<Article> findByIsFeaturedTrueAndIsAcceptedTrue();

    // Ultimo articolo accettato
    Article findTopByIsAcceptedTrueOrderByPublishDateDesc();

    // Ultimo articolo featured accettato
    Article findTopByIsFeaturedTrueAndIsAcceptedTrueOrderByPublishDateDesc();

    // Secondo articolo accettato (serve se non ci sono featured)
    List<Article> findTop2ByIsAcceptedTrueOrderByPublishDateDesc();

    // Trova gli ultimi 4 articoli accettati
    List<Article> findTop4ByIsAcceptedTrueOrderByPublishDateDesc();

    // Trova articoli per tipo
    List<Article> findByArticleTypeAndIsAcceptedTrue(String articleType);

    // ricerca per titolo, sottotitolo, autore e categoria
    @Query("SELECT a FROM Article a WHERE " +
            "LOWER(a.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.subtitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.user.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(a.category.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Article> search(@Param("searchTerm") String searchTerm);

}
