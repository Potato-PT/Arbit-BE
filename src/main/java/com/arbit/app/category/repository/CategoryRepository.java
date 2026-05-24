package com.arbit.app.category.repository;

import com.arbit.app.category.entity.Category;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByNameIn(Collection<String> names);
}
