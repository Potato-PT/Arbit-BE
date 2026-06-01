package com.arbit.app.event.repository;

import com.arbit.app.event.entity.EventKeyword;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventKeywordRepository extends JpaRepository<EventKeyword, Long> {

    @Query("""
            select ek.preferenceKeyword.value
            from EventKeyword ek
            where ek.event.id = :eventId
            order by ek.preferenceKeyword.value
            """)
    List<String> findKeywordValuesByEventId(@Param("eventId") UUID eventId);
}
