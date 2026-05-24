package com.arbit.app.keyword.repository;

import com.arbit.app.keyword.entity.UserPreferenceKeyword;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserPreferenceKeywordRepository extends JpaRepository<UserPreferenceKeyword, Long> {

    void deleteAllByUserId(UUID userId);

    @Query("""
            select upk
            from UserPreferenceKeyword upk
            join fetch upk.preferenceKeyword pk
            where upk.user.id = :userId
            order by pk.value asc
            """)
    List<UserPreferenceKeyword> findAllByUserIdWithKeyword(@Param("userId") UUID userId);
}
