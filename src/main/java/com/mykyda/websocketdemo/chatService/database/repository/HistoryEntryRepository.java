package com.mykyda.websocketdemo.chatService.database.repository;

import com.mykyda.websocketdemo.chatService.database.entity.HistoryEntry;
import com.mykyda.websocketdemo.chatService.database.entity.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryEntryRepository extends JpaRepository<HistoryEntry, Long> {

    Page<HistoryEntry> findAllByChatIdAndStatusNotOrderByTimestampDesc(Long chatId, MessageStatus status ,Pageable pageable);

    List<HistoryEntry> findAllByChatIdAndStatusOrderByTimestampDesc(Long chatId, MessageStatus status);

    Page<HistoryEntry> findAllByChatIdOrderByTimestampDesc(Long chatId, Pageable pageable);

    @Modifying
    @Query("UPDATE HistoryEntry h SET h.status = :status WHERE h.id  in :ids")
    void updateStatusByIds(@Param("ids") List<Long> id, @Param("status") MessageStatus status);
}