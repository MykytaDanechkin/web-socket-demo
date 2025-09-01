package com.mykyda.websocketdemo.chatService.dto;

import com.mykyda.websocketdemo.chatService.database.entity.HistoryEntry;
import com.mykyda.websocketdemo.chatService.database.entity.MessageStatus;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.sql.Timestamp;

@Value
@FieldNameConstants
@Getter
@Setter
@ToString
@Builder
public class HistoryEntryDTO {

    Long id;

    String content;

    Long chatId;

    Long senderId;

    Timestamp timestamp;

    MessageStatus status;

    public static HistoryEntryDTO of(HistoryEntry historyEntry) {
        return (historyEntry == null) ? null : new HistoryEntryDTO(
                historyEntry.getId(),
                historyEntry.getContent(),
                historyEntry.getChat().getId(),
                historyEntry.getSendersId(),
                historyEntry.getTimestamp(),
                historyEntry.getStatus()
        );
    }
}
