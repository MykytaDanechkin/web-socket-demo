package com.mykyda.websocketdemo.chatService.dto;

import com.mykyda.websocketdemo.chatService.database.entity.Chat;
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
        var last = chat.getLastMessage();
        HistoryEntryDto lastDto;
        if (last == null) {
            lastDto = null;
        } else {
            lastDto = HistoryEntryDto.of(last);
        }
        return new ChatDTO(chat.getId(), UserDTO.of(chat.getUser1()), UserDTO.of(chat.getUser2()), lastDto);
    }
}
