package com.mykyda.websocketdemo.dto;

import com.mykyda.websocketdemo.database.entity.Chat;
import com.mykyda.websocketdemo.database.entity.HistoryEntry;
import com.mykyda.websocketdemo.security.dto.UserDTO;
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

    UserDTO user1;

    UserDTO user2;

    HistoryEntryDto lastMessage;

    public static ChatDTO of(Chat chat) {
        return new ChatDTO(chat.getId(), UserDTO.of(chat.getUser1()), UserDTO.of(chat.getUser2()), HistoryEntryDto.of(chat.getLastMessage()));
    }
}
