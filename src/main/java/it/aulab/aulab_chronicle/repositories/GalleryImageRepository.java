package it.aulab.aulab_chronicle.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.aulab.aulab_chronicle.models.GalleryImage;
// import jakarta.transaction.Transactional;

public interface GalleryImageRepository extends JpaRepository<GalleryImage, Long> {

    @Modifying
    @Query(value = "DELETE FROM gallery_images WHERE path = :path", nativeQuery = true)
    void deleteByPath(@Param("path") String path);

    @Query(value = "SELECT * FROM gallery_images WHERE article_id = :articleId", nativeQuery = true)
    List<GalleryImage> findByArticleId(@Param("articleId") Long articleId);

}