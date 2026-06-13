package com.arbit.app.keyword.repository;

import com.arbit.app.keyword.entity.Keyword;
import com.arbit.app.keyword.entity.KeywordType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    Optional<Keyword> findByTypeAndValue(KeywordType type, String value);
}
