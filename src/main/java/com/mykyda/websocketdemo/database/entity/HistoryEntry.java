package com.mykyda.websocketdemo.database.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name= "history_entry" )
@Builder
public class HistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Chat chatId;

    private String sendersEmail;

    @Builder.Default
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());
}
