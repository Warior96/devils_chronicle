package it.aulab.aulab_chronicle;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
// import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import it.aulab.aulab_chronicle.models.Article;
import it.aulab.aulab_chronicle.models.Category;
import it.aulab.aulab_chronicle.models.User;
import it.aulab.aulab_chronicle.repositories.ArticleRepository;
import it.aulab.aulab_chronicle.repositories.CategoryRepository;
import it.aulab.aulab_chronicle.repositories.UserRepository;
import jakarta.transaction.Transactional;

// @SpringBootTest
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class AulabChronicleApplicationTests {

	@Autowired
	private ArticleRepository articleRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private UserRepository userRepository;

	private Category category;

	private User user;

	private Article article;

	@BeforeEach
	void setup() {
		// Creo e salvo categoria di test
		category = new Category();
		category.setName("Categoria Test");
		category = categoryRepository.save(category);

		// Creo e salvo utente di test
		user = new User();
		user.setUsername("utenteTest");
		user.setEmail("utente@test.it");
		user.setPassword("password123");
		user = userRepository.save(user);

		// Creo articolo senza cover e senza gallery
		article = new Article();
		article.setTitle("Titolo Test");
		article.setSubtitle("Sottotitolo Test");
		article.setBody("Testo dell'articolo...");
		article.setPublishDate(LocalDate.now());
		article.setCategory(category);
		article.setUser(user);
		article.setIsAccepted(true);
		article = articleRepository.save(article);
	}

	@Test
	void testCreateArticleWithoutMedia() {
		assertThat(article.getId()).isNotNull();
		assertThat(article.getImage()).isNull();
		assertThat(article.getGalleryImages()).isEmpty();
	}

	@Test
	void testFindByCategory() {
		List<Article> articles = articleRepository.findByCategory(category);
		assertThat(articles).isNotEmpty();
	}

	@Test
	void testUpdateArticleWithoutMedia() {
		article.setTitle("Titolo aggiornato");
		article.setBody("Nuovo corpo articolo");
		articleRepository.save(article);

		Optional<Article> updated = articleRepository.findById(article.getId());
		assertThat(updated).isPresent();
		assertThat(updated.get().getTitle()).isEqualTo("Titolo aggiornato");
		assertThat(updated.get().getImage()).isNull();
		assertThat(updated.get().getGalleryImages()).isEmpty();
	}

	@Test
	void testDeleteArticleWithoutMedia() {
		articleRepository.delete(article);
		Optional<Article> deleted = articleRepository.findById(article.getId());
		assertThat(deleted).isEmpty();
	}

}
