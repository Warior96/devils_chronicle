CREATE TABLE gallery_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    path VARCHAR(255) NOT NULL,
    article_id BIGINT,
    FOREIGN KEY (article_id) REFERENCES articles(id)
);
