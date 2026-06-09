package com.arbit.app.event.repository;

import com.arbit.app.event.entity.EventDetailViewLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventDetailViewLogRepository extends JpaRepository<EventDetailViewLog, Long> {

    List<EventDetailViewLog> findAllByUserId(UUID userId);

    void deleteAllByUserId(UUID userId);
}
