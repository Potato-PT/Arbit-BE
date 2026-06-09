package com.arbit.app.event.repository;

import com.arbit.app.event.entity.EventActionLog;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventActionLogRepository extends JpaRepository<EventActionLog, Long> {

    void deleteAllByUserId(UUID userId);
}
