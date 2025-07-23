package com.mykyda.websocketdemo.database.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @Lob
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    private String sendersEmail;

    @Builder.Default
    private Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageStatus status = MessageStatus.UNSEEN;

    @Override
    public String toString() {
        return "HistoryEntry{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", sendersEmail='" + sendersEmail + '\'' +
                ", timestamp=" + timestamp +
                ", status=" + status +
                ", chatId=" + chat.getId() +
                '}';
    }
}
