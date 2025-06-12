package it.aulab.aulab_chronicle.services;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import it.aulab.aulab_chronicle.dtos.CategoryDto;
import it.aulab.aulab_chronicle.models.Article;
import it.aulab.aulab_chronicle.models.Category;
import it.aulab.aulab_chronicle.repositories.CategoryRepository;
import jakarta.transaction.Transactional;

@Service
public class CategoryService implements CrudService<CategoryDto, Category, Long> {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<CategoryDto> readAll() {

        List<CategoryDto> dtos = new ArrayList<CategoryDto>();
        for (Category category : categoryRepository.findAll()) {
            dtos.add(modelMapper.map(category, CategoryDto.class));
        }
        return dtos;

    }

    @Override
    public CategoryDto read(Long key) {

        return modelMapper.map(categoryRepository.findById(key).get(), CategoryDto.class);

    }

    @Override
    public CategoryDto create(Category model, Principal principal, MultipartFile file, MultipartFile[] galleryFiles) {

        return modelMapper.map(categoryRepository.save(model), CategoryDto.class);

    }

    @Override
    public CategoryDto update(Long key, Category model, MultipartFile file, MultipartFile[] galleryFiles) {

        if (categoryRepository.existsById(key)) {
            return modelMapper.map(categoryRepository.save(model), CategoryDto.class);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }

    @Override
    @Transactional
    public void delete(Long id) {

        Category category = categoryRepository.findById(id).get();

        if (category.getArticles() != null) {

            Iterable<Article> articles = category.getArticles();
            for (Article article : articles) {
                article.setCategory(null);
            }

            categoryRepository.deleteById(id);

        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

    }

}
