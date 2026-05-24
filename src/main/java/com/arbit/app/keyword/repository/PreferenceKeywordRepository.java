package com.arbit.app.keyword.repository;

import com.arbit.app.keyword.entity.PreferenceKeyword;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferenceKeywordRepository extends JpaRepository<PreferenceKeyword, Long> {

    List<PreferenceKeyword> findByValueIn(Collection<String> values);
}
