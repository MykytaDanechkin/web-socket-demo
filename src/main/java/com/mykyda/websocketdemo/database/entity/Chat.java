package com.mykyda.websocketdemo.database.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mykyda.websocketdemo.security.database.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "chat",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user1_id", "user2_id"})
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User user2;

    public static Chat of(User user1, User user2) {
        if (user1.getId() == Math.min(user1.getId(), user2.getId())) {
            return Chat.builder()
                    .user1(user1)
                    .user2(user2)
                    .build();
        } else {
            return Chat.builder()
                    .user1(user2)
                    .user2(user1)
                    .build();
        }
    }

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private HistoryEntry lastMessage;
}
