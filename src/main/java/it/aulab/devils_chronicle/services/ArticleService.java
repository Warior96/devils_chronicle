package it.aulab.devils_chronicle.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import it.aulab.devils_chronicle.dtos.ArticleDto;
import it.aulab.devils_chronicle.models.Article;
import it.aulab.devils_chronicle.models.Category;
import it.aulab.devils_chronicle.models.GalleryImage;
import it.aulab.devils_chronicle.models.User;
import it.aulab.devils_chronicle.repositories.ArticleRepository;
import it.aulab.devils_chronicle.repositories.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class ArticleService implements CrudService<ArticleDto, Article, Long> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ImageService imageService;

    @Override
    public List<ArticleDto> readAll() {

        List<ArticleDto> dtos = new ArrayList<ArticleDto>();
        for (Article article : articleRepository.findAll()) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;

    }

    @Override
    public ArticleDto read(Long key) {
        Optional<Article> optArticle = articleRepository.findById(key);
        if (optArticle.isPresent()) {
            return modelMapper.map(optArticle.get(), ArticleDto.class);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Article id-" + key + " not found");
        }
    }

    @Transactional
    @Override
    public ArticleDto create(Article article, Principal principal, MultipartFile file, MultipartFile[] galleryFiles) {

        String url = "";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = (userRepository.findById(userDetails.getId()).get());
            article.setUser(user);
        }

        if (!file.isEmpty()) {
            try {
                CompletableFuture<String> futureUrl = imageService.saveImageOnCloud(file);
                url = futureUrl.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        article.setIsAccepted(null);

        ArticleDto dto = modelMapper.map(articleRepository.save(article), ArticleDto.class);

        if (!file.isEmpty()) {
            imageService.saveImageOnDB(url, article, true);
        }

        if (galleryFiles != null) {
            for (MultipartFile galleryFile : galleryFiles) {
                if (!galleryFile.isEmpty()) {
                    try {
                        String galleryUrl = imageService.saveImageOnCloud(galleryFile).get();
                        imageService.saveImageOnDB(galleryUrl, article, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return dto;

    }

    @Transactional
    @Override
    public ArticleDto update(Long key, Article updatedArticle, MultipartFile file, MultipartFile[] galleryFiles) {

        String url = "";

        // se l'articolo esiste
        if (articleRepository.existsById(key)) {

            // recupero l'articolo originale senza modifiche
            Article article = articleRepository.findById(key).get();
            // assegno all'articolo del form, l'id dell'articolo originale
            updatedArticle.setId(key);
            // assegno all'articolo del form, l'autore dell'articolo originale
            updatedArticle.setUser(article.getUser());

            // se è presente una nuova immagine nel form
            if (!file.isEmpty()) {
                try {
                    // elimino l'immagine dell'articolo originale dal cloud
                    imageService.deleteImage(article.getImage().getPath(), true);
                    try {
                        // salvo l'immagine nuova del form nel cloud
                        CompletableFuture<String> futureUrl = imageService.saveImageOnCloud(file);
                        url = futureUrl.get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // salvo il nuovo path dell'immagine nel db
                    imageService.saveImageOnDB(url, updatedArticle, true);
                    // articolo torna in revisione
                    updatedArticle.setIsAccepted(null);

                    return modelMapper.map(articleRepository.save(updatedArticle), ArticleDto.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (article.getImage() == null) { // se l'articolo originale non ha immagine e il form non
                                                     // ha immagine
                updatedArticle.setIsAccepted(article.getIsAccepted());

            } else { // se il form NON ha immagine
                // assegno all'articolo del form, l'immagine dell'articolo originale
                updatedArticle.setImage(article.getImage());

                // se il form ha modifiche
                if (updatedArticle.equals(article) == false) {
                    // articolo ha modifiche quindi torna in revisione
                    updatedArticle.setIsAccepted(null);
                } else {
                    // articolo NON ha modifiche quindi NON torna in revisione
                    updatedArticle.setIsAccepted(article.getIsAccepted());
                }

                // gallery
                if (galleryFiles != null && galleryFiles.length > 0) {
                    try {
                        // elimina dal cloud tutte le immagini della galleria esistenti
                        if (article.getGalleryImages() != null && !article.getGalleryImages().isEmpty()) {
                            for (GalleryImage img : article.getGalleryImages()) {
                                try {
                                    imageService.deleteImage(img.getPath(), false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        // salva nuove immagini di galleria
                        for (MultipartFile galleryFile : galleryFiles) {
                            if (!galleryFile.isEmpty()) {
                                try {
                                    String galleryUrl = imageService.saveImageOnCloud(galleryFile).get();
                                    imageService.saveImageOnDB(galleryUrl, updatedArticle, false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        // poiché galleria modificata, resetto lo stato di accettazione
                        updatedArticle.setIsAccepted(null);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return modelMapper.map(articleRepository.save(updatedArticle), ArticleDto.class);
            }

        } else { // articolo non esiste
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return null;

    }

    @Transactional
    @Override
    public void delete(Long key) {

        if (articleRepository.existsById(key)) {
            Article article = articleRepository.findById(key).get();

            // elimino la cover dell'articolo
            if (article.getImage() != null) {
                try {
                    String path = article.getImage().getPath();
                    article.getImage().setArticle(null);
                    article.setImage(null);
                    imageService.deleteImage(path, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // elimino tutte le immagini della galleria
            if (article.getGalleryImages() != null && !article.getGalleryImages().isEmpty()) {
                for (GalleryImage img : article.getGalleryImages()) {
                    try {
                        String path = img.getPath();
                        img.setArticle(null); // disaccoppia l'immagine
                        imageService.deleteImage(path, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                article.setGalleryImages(null);
            }

            articleRepository.deleteById(key);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }

    public List<ArticleDto> searchByCategory(Category category) {

        List<ArticleDto> dtos = new ArrayList<ArticleDto>();
        for (Article article : articleRepository.findByCategory(category)) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;

    }

    public List<ArticleDto> searchByAuthor(User user) {

        List<ArticleDto> dtos = new ArrayList<ArticleDto>();
        for (Article article : articleRepository.findByUser(user)) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;

    }

    public void setIsAccepted(Boolean result, Long id) {
        Article article = articleRepository.findById(id).get();
        article.setIsAccepted(result);
        articleRepository.save(article);
    }

    public List<ArticleDto> search(String keyword) {
        List<ArticleDto> dtos = new ArrayList<ArticleDto>();
        for (Article article : articleRepository.search(keyword)) {
            dtos.add(modelMapper.map(article, ArticleDto.class));
        }
        return dtos;
    }

}