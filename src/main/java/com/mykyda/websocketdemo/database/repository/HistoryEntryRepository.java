package com.mykyda.websocketdemo.database.repository;

import com.mykyda.websocketdemo.database.entity.HistoryEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryEntryRepository extends JpaRepository<HistoryEntry, Long> {

    Page<HistoryEntry> findAllByChatIdOrderByTimestampDesc(Long chatId, Pageable pageable);

    List<HistoryEntry> findAllByChatIdOrderByTimestampDesc(Long chatId);
}
