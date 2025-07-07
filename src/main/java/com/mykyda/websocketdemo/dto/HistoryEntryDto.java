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

    String content;

    String sendersEmail;

    Timestamp timestamp;

    public static HistoryEntryDto entryToDto(HistoryEntry entity) {
        return new HistoryEntryDto(entity.getContent(), entity.getSendersEmail(), entity.getTimestamp());
    }
}
