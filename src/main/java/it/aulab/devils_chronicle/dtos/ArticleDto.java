package it.aulab.devils_chronicle.dtos;

import java.time.LocalDate;
import java.util.List;

import it.aulab.devils_chronicle.models.Category;
import it.aulab.devils_chronicle.models.GalleryImage;
import it.aulab.devils_chronicle.models.Image;
import it.aulab.devils_chronicle.models.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ArticleDto {

    private Long id;
    private String title;
    private String subtitle;
    private String body;
    private LocalDate publishDate;
    private Boolean isAccepted;
    private User user;
    private Category category;
    private Image image;
    private List<GalleryImage> galleryImages;


}
