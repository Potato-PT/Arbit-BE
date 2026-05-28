package com.arbit.app.event.repository;

import com.arbit.app.event.entity.EventAgeRestriction;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventAgeRestrictionRepository extends JpaRepository<EventAgeRestriction, Long> {

    Optional<EventAgeRestriction> findByEventId(UUID eventId);
}
