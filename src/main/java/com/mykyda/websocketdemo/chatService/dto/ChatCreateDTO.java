package com.mykyda.websocketdemo.chatService.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatCreateDTO {

    private Long targetUserId;

    private Long currentUserId;

    private String initialMessage;

    private String targetEmail;
}
