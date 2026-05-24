package com.arbit.app.category.repository;

import com.arbit.app.category.entity.UserCategory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCategoryRepository extends JpaRepository<UserCategory, Long> {

    void deleteAllByUserId(UUID userId);
}
