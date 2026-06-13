package com.arbit.app.keyword.repository;

import com.arbit.app.keyword.entity.UserKeywordWeight;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserKeywordWeightRepository extends JpaRepository<UserKeywordWeight, Long> {

    void deleteAllByUserId(UUID userId);
}
