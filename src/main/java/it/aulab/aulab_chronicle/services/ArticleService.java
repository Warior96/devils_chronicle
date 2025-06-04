package it.aulab.aulab_chronicle.services;

import java.security.Principal;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import it.aulab.aulab_chronicle.dtos.ArticleDto;
import it.aulab.aulab_chronicle.models.Article;
import it.aulab.aulab_chronicle.models.User;
import it.aulab.aulab_chronicle.repositories.ArticleRepository;
import it.aulab.aulab_chronicle.repositories.UserRepository;

@Service
public class ArticleService implements CrudService<ArticleDto, Article, Long> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public List<ArticleDto> readAll() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'readAll'");
    }

    @Override
    public ArticleDto read(Long key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'read'");
    }

    @Override
    public ArticleDto create(Article article, Principal principal, MultipartFile file) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = (userRepository.findById(userDetails.getId()).get());
            article.setUser(user);
        }
        ArticleDto dto = modelMapper.map(articleRepository.save(article), ArticleDto.class);
        return dto;

    }

    @Override
    public ArticleDto update(Long key, Article model, MultipartFile file) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(Long key) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

}
