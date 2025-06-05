package it.aulab.aulab_chronicle.dtos;

import java.time.LocalDate;

import it.aulab.aulab_chronicle.models.Category;
import it.aulab.aulab_chronicle.models.Image;
import it.aulab.aulab_chronicle.models.User;
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
    private User user;
    private Category category;
    private Image image;

}
