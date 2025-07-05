package com.mykyda.websocketdemo.database.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "user1_id")
    private Long user1Id;

    @Column(name = "user2_id")
    private Long user2Id;

    public static Chat of(Long userId1, Long userId2) {
        var a = Math.min(userId1, userId2);
        var b = Math.max(userId1, userId2);
        return Chat.builder()
                .user1Id(a)
                .user2Id(b)
                .build();
    }
}
