package com.atm.notification.repository;

import com.atm.notification.entity.WebSocketSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebSocketSessionRepository extends JpaRepository<WebSocketSession, Long> {
    
    List<WebSocketSession> findByUserId(Long userId);
    
    Optional<WebSocketSession> findBySessionId(String sessionId);
    
    void deleteBySessionId(String sessionId);
    
    @Query("SELECT DISTINCT w.userId FROM WebSocketSession w")
    List<Long> findDistinctUserIds();
}
