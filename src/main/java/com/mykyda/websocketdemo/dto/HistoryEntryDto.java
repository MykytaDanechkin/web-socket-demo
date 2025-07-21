package com.mykyda.websocketdemo.dto;

import com.mykyda.websocketdemo.database.entity.HistoryEntry;
import com.mykyda.websocketdemo.database.entity.MessageStatus;
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

    Long chatId;

    String sendersEmail;

    Timestamp timestamp;

    MessageStatus status;

    public static HistoryEntryDto of(HistoryEntry historyEntry) {
        return (historyEntry == null) ? null : new HistoryEntryDto(historyEntry.getId(), historyEntry.getContent(), historyEntry.getChat().getId() ,historyEntry.getSendersEmail(), historyEntry.getTimestamp(), historyEntry.getStatus());
    }
}
