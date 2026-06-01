package com.arbit.app.preference.repository;

import com.arbit.app.preference.entity.UserPreferenceEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceEventRepository extends JpaRepository<UserPreferenceEvent, Long> {

    void deleteAllByUserId(UUID userId);

    List<UserPreferenceEvent> findAllByUserId(UUID userId);
}
