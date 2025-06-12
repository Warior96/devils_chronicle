package it.aulab.aulab_chronicle;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import it.aulab.aulab_chronicle.models.Article;
import it.aulab.aulab_chronicle.models.Category;
import it.aulab.aulab_chronicle.models.GalleryImage;
import it.aulab.aulab_chronicle.models.Image;
import it.aulab.aulab_chronicle.models.User;
import it.aulab.aulab_chronicle.repositories.ArticleRepository;
import it.aulab.aulab_chronicle.repositories.CategoryRepository;
import it.aulab.aulab_chronicle.repositories.GalleryImageRepository;
import it.aulab.aulab_chronicle.repositories.UserRepository;
import jakarta.transaction.Transactional;

import java.util.ArrayList;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class ArticleWithMediaTests {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GalleryImageRepository galleryImageRepository;

    private Category category;
    private User user;
    private Article article;

    @BeforeEach
    void setup() {
        category = new Category();
        category.setName("Categoria Test");
        category = categoryRepository.save(category);

        user = new User();
        user.setUsername("utenteTest");
        user.setEmail("utente@test.it");
        user.setPassword("password123");
        user = userRepository.save(user);

        Image image = Image.builder().path("cover.jpg").build();

        GalleryImage g1 = GalleryImage.builder().path("gallery1.jpg").build();
        GalleryImage g2 = GalleryImage.builder().path("gallery2.jpg").build();

        List<GalleryImage> gallery = new ArrayList<>();
        gallery.add(g1);
        gallery.add(g2);

        article = new Article();
        article.setTitle("Titolo");
        article.setSubtitle("Sottotitolo");
        article.setBody("Contenuto");
        article.setPublishDate(LocalDate.now());
        article.setIsAccepted(true);
        article.setCategory(category);
        article.setUser(user);
        article.setImage(image);
        article.setGalleryImages(gallery);

        image.setArticle(article);
        g1.setArticle(article);
        g2.setArticle(article);

        article = articleRepository.save(article);
    }

    @Test
    void testCreateArticleWithMedia() {
        assertThat(article.getId()).isNotNull();
        assertThat(article.getImage()).isNotNull();
        assertThat(article.getGalleryImages()).hasSize(2);
    }

    @Test
    void testUpdateArticleWithNewMedia() {

        article.setImage(null);

        // nuova cover
        Image newCover = Image.builder().path("new-cover.jpg").article(article).build();
        article.setImage(newCover);

        // eliminare galleria dal DB
        List<GalleryImage> oldGallery = article.getGalleryImages();
        galleryImageRepository.deleteAll(oldGallery);
        article.setGalleryImages(new ArrayList<>());

        // nuove immagini di galleria
        GalleryImage g3 = GalleryImage.builder().path("gallery3.jpg").article(article).build();
        galleryImageRepository.save(g3);
        GalleryImage g4 = GalleryImage.builder().path("gallery4.jpg").article(article).build();
        galleryImageRepository.save(g4);
        List<GalleryImage> newGallery = new ArrayList<>();
        newGallery.add(g3);
        newGallery.add(g4);
        article.setGalleryImages(newGallery);

        article = articleRepository.save(article);

        Optional<Article> updated = articleRepository.findById(article.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getImage()).isNotNull();
        assertThat(updated.get().getImage().getPath()).isEqualTo("new-cover.jpg");
        assertThat(updated.get().getGalleryImages()).hasSize(2);
        assertThat(updated.get().getGalleryImages().get(0).getPath()).isEqualTo("gallery3.jpg");
        assertThat(updated.get().getGalleryImages().get(1).getPath()).isEqualTo("gallery4.jpg");
    }

    @Test
    void testDeleteArticleWithMedia() {
        Long id = article.getId();
        articleRepository.delete(article);
        assertThat(articleRepository.findById(id)).isEmpty();
    }

}
