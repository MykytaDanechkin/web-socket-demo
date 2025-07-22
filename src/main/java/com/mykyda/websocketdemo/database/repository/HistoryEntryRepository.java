package com.mykyda.websocketdemo.database.repository;

import com.mykyda.websocketdemo.database.entity.HistoryEntry;
import com.mykyda.websocketdemo.database.entity.MessageStatus;
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

    Page<HistoryEntry> findAllByChatIdOrderByTimestampDesc(Long chatId, Pageable pageable);

    List<HistoryEntry> findAllByChatIdOrderByTimestampDesc(Long chatId);

    @Modifying
    @Query("UPDATE HistoryEntry h SET h.status = :status WHERE h.id  in :ids")
    void updateStatusByIds(@Param("ids") List<Long> id, @Param("status") MessageStatus status);
}