package com.events.management.repository;

import com.events.management.entity.Event;
import com.events.management.entity.Ticket;
import com.events.management.entity.TicketStatus;
import com.events.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketCode(String ticketCode);

    List<Ticket> findByUser(User user);

    List<Ticket> findByEvent(Event event);

    List<Ticket> findByUserAndEvent(User user, Event event);

    List<Ticket> findByEventAndStatus(Event event, TicketStatus status);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.event = :event " +
           "AND t.status != 'CANCELLED'")
    long countValidTicketsByEvent(@Param("event") Event event);

    @Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.user = :user " +
           "AND t.event = :event AND t.status = 'VALID'")
    boolean hasValidTicket(@Param("user") User user, @Param("event") Event event);
}