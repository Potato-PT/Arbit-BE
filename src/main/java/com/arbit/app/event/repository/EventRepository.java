package com.arbit.app.event.repository;

import com.arbit.app.event.entity.Event;
import com.arbit.app.event.entity.EventStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, UUID> {

    @EntityGraph(attributePaths = "category")
    List<Event> findByStatusOrderByEndDateAsc(EventStatus status);
}
