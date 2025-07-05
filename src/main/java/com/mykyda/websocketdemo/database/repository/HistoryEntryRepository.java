package com.mykyda.websocketdemo.database.repository;

import com.mykyda.websocketdemo.database.entity.HistoryEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryEntryRepository extends JpaRepository<HistoryEntry, Long> {
}
