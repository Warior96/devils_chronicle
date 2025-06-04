package it.aulab.aulab_chronicle.repositories;

import org.springframework.data.repository.ListCrudRepository;

import it.aulab.aulab_chronicle.models.Category;

public interface CategoryRepository extends ListCrudRepository<Category, Long> {

}
