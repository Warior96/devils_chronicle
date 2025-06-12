package it.aulab.aulab_chronicle.services;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.multipart.MultipartFile;

import it.aulab.aulab_chronicle.models.Article;

public interface ImageService {

    void saveImageOnDB(String url, Article article, boolean isCover);

    CompletableFuture<String> saveImageOnCloud(MultipartFile file) throws Exception;

    void deleteImage(String imagePath, boolean isCover) throws IOException;

}
