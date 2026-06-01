package com.arbit.app.event.repository;

import com.arbit.app.event.entity.Event;
import com.arbit.app.event.entity.EventStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, UUID> {

    @EntityGraph(attributePaths = "category")
    Optional<Event> findWithCategoryById(UUID id);

    @EntityGraph(attributePaths = "category")
    @Query("""
            select e
            from Event e
            where e.status = :status
              and (:category is null or e.category.name = :category)
              and (:filterByDistrict = false or e.district in :districts)
              and (:startDate is null or e.startDate >= :startDate)
              and (:endDate is null or e.endDate <= :endDate)
            order by
              case when :sort = 'rating' then e.averageRating end desc,
              case when :sort = 'latest' then e.startDate end desc,
              e.endDate asc
            """)
    List<Event> findByStatusOrderByEndDateAsc(
            @Param("status") EventStatus status,
            @Param("category") String category,
            @Param("filterByDistrict") boolean filterByDistrict,
            @Param("districts") List<String> districts,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("sort") String sort);

    @EntityGraph(attributePaths = "category")
    List<Event> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "category")
    List<Event> findByStatusNotOrderByCreatedAtDesc(EventStatus status);
}
