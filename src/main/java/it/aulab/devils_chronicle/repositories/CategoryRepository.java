package it.aulab.devils_chronicle.repositories;

import org.springframework.data.repository.ListCrudRepository;

import it.aulab.devils_chronicle.models.Category;

public interface CategoryRepository extends ListCrudRepository<Category, Long> {

}
