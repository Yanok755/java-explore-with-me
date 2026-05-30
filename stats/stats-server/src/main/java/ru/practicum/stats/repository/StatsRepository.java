package ru.practicum.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.stats.model.Hit;
import ru.practicum.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsRepository extends JpaRepository<Hit, Long> {

    @Query("SELECT new ru.practicum.stats.dto.ViewStats(h.app, h.uri, COUNT(h.id)) " +
           "FROM Hit h " +
           "WHERE h.timestamp BETWEEN :start AND :end " +
           "AND (COALESCE(:uris, NULL) IS NULL OR h.uri IN :uris) " +
           "GROUP BY h.app, h.uri " +
           "ORDER BY COUNT(h.id) DESC")
    List<ViewStats> getStats(@Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end,
                             @Param("uris") List<String> uris);

    @Query("SELECT new ru.practicum.stats.dto.ViewStats(h.app, h.uri, COUNT(DISTINCT h.ip)) " +
           "FROM Hit h " +
           "WHERE h.timestamp BETWEEN :start AND :end " +
           "AND (COALESCE(:uris, NULL) IS NULL OR h.uri IN :uris) " +
           "GROUP BY h.app, h.uri " +
           "ORDER BY COUNT(DISTINCT h.ip) DESC")
    List<ViewStats> getUniqueStats(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end,
                                   @Param("uris") List<String> uris);
}
