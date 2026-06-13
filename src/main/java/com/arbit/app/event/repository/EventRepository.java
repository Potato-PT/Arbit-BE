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
            where (:filterByStatus = false
                or (:ongoing = true
                    and e.startDate <= :today and e.endDate >= :today)
                or (:upcoming = true
                    and e.startDate > :today)
              )
              and (:category is null or e.category.name = :category)
              and (:filterByDistrict = false or e.district in :districts)
              and (:startDate is null or e.startDate >= :startDate)
              and (:endDate is null or e.endDate <= :endDate)
              and (:isFree is null or e.free = :isFree)
            order by
              case when :sort = 'rating' then e.averageRating end desc,
              case when :sort = 'latest' then e.startDate end desc,
              e.endDate asc
            """)
    List<Event> findByFiltersOrderByEndDateAsc(
            @Param("filterByStatus") boolean filterByStatus,
            @Param("ongoing") boolean ongoing,
            @Param("upcoming") boolean upcoming,
            @Param("today") LocalDate today,
            @Param("category") String category,
            @Param("filterByDistrict") boolean filterByDistrict,
            @Param("districts") List<String> districts,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("isFree") Boolean isFree,
            @Param("sort") String sort);

    @EntityGraph(attributePaths = "category")
    List<Event> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = "category")
    List<Event> findByStatusNotOrderByCreatedAtDesc(EventStatus status);

    @EntityGraph(attributePaths = "category")
    @Query("""
            select distinct e
            from Event e
            left join EventKeyword ek on ek.event = e
            left join ek.preferenceKeyword pk
            where (:keyword is null
                or (:target = 'ALL' and (
                    lower(e.title) like lower(concat('%', :keyword, '%'))
                    or lower(e.category.name) like lower(concat('%', :keyword, '%'))
                    or lower(e.venue) like lower(concat('%', :keyword, '%'))
                    or lower(e.district) like lower(concat('%', :keyword, '%'))
                    or lower(pk.value) like lower(concat('%', :keyword, '%'))
                ))
                or (:target = 'TITLE' and lower(e.title) like lower(concat('%', :keyword, '%')))
                or (:target = 'CATEGORY' and lower(e.category.name) like lower(concat('%', :keyword, '%')))
                or (:target = 'VENUE' and lower(e.venue) like lower(concat('%', :keyword, '%')))
                or (:target = 'DISTRICT' and lower(e.district) like lower(concat('%', :keyword, '%')))
                or (:target = 'KEYWORD' and lower(pk.value) like lower(concat('%', :keyword, '%')))
            )
              and (:category is null or e.category.name = :category)
              and (:filterByDistrict = false or e.district in :districts)
              and (:filterByStatus = false
                or (:ongoing = true
                    and e.startDate <= :today and e.endDate >= :today)
                or (:upcoming = true
                    and e.startDate > :today)
                or (:closed = true
                    and e.endDate < :today)
              )
              and (:free is null or e.free = :free)
            """)
    List<Event> searchEvents(
            @Param("keyword") String keyword,
            @Param("target") String target,
            @Param("category") String category,
            @Param("filterByDistrict") boolean filterByDistrict,
            @Param("districts") List<String> districts,
            @Param("filterByStatus") boolean filterByStatus,
            @Param("ongoing") boolean ongoing,
            @Param("upcoming") boolean upcoming,
            @Param("closed") boolean closed,
            @Param("free") Boolean free,
            @Param("today") LocalDate today);

    @Query("""
            select pk.value
            from EventKeyword ek
            join ek.preferenceKeyword pk
            where ek.event.id = :eventId
              and lower(pk.value) like lower(concat('%', :keyword, '%'))
            order by pk.value
            """)
    List<String> findMatchedKeywordValues(@Param("eventId") UUID eventId, @Param("keyword") String keyword);
}
