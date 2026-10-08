package com.example.event.repository;

import com.example.event.model.entity.Event;
import com.example.event.model.enums.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("select e from Event e where (:type is null or e.type = :type)")
    Page<Event> search(@Param("type") EventType type, Pageable pageable);
}
