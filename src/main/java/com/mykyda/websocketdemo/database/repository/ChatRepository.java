package com.mykyda.websocketdemo.database.repository;

import com.mykyda.websocketdemo.database.entity.Chat;
import com.mykyda.websocketdemo.database.entity.HistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    Optional<Chat> findByUser1IdAndUser2Id(Long user1Id, Long user2Id);

    List<Chat> findAllByUser1IdOrUser2Id(Long user1Id, Long user2Id);

    @Modifying
    @Query("UPDATE Chat c SET c.lastMessage = :lastMessage WHERE c.id = :chatId")
    void updateLastMessage(@Param("chatId") Long chatId, @Param("lastMessage") HistoryEntry lastMessage);
}
