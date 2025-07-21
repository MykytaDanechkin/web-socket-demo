package com.mykyda.websocketdemo.dto;

import com.mykyda.websocketdemo.database.entity.HistoryEntry;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.FieldNameConstants;

import java.sql.Timestamp;

@Value
@FieldNameConstants
@Getter
@Setter
public class HistoryEntryDto {

    Long id;

    String content;

    String sendersEmail;

    Timestamp timestamp;

    public static HistoryEntryDto of(HistoryEntry historyEntry) {
        return (historyEntry == null) ? null : new HistoryEntryDto(historyEntry.getId(), historyEntry.getContent(), historyEntry.getSendersEmail(), historyEntry.getTimestamp());
    }
}
