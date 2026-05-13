package com.events.management.repository;

import com.events.management.entity.Event;
import com.events.management.entity.EventCategory;
import com.events.management.entity.EventStatus;
import com.events.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByOrganizer(User organizer);

    List<Event> findByStatus(EventStatus status);

    List<Event> findByCategory(EventCategory category);

    List<Event> findByAvailableSeatsGreaterThan(Integer seats);

    List<Event> findByTitleContainingIgnoreCase(String keyword);

    @Query("SELECT e FROM Event e WHERE e.startDate > :now " +
           "AND e.status = 'ACTIVE' ORDER BY e.startDate ASC")
    List<Event> findUpcomingEvents(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM Event e WHERE " +
           "(:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:status IS NULL OR e.status = :status) " +
           "ORDER BY e.startDate DESC")
    List<Event> searchEvents(
        @Param("keyword") String keyword,
        @Param("category") EventCategory category,
        @Param("status") EventStatus status
    );
}