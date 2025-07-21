package com.mykyda.websocketdemo.dto;

import com.mykyda.websocketdemo.database.entity.Chat;
import com.mykyda.websocketdemo.database.entity.HistoryEntry;
import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Data
@AllArgsConstructor
@ToString
@Setter
@EqualsAndHashCode
public class ChatDTO {

    Long id;

    UserDto user1;

    UserDto user2;

    HistoryEntry lastMessage;

    public static ChatDTO of(Chat chat) {
        return new ChatDTO(chat.getId(), UserDto.of(chat.getUser1()), UserDto.of(chat.getUser2()), chat.getLastMessage());
    }
}
