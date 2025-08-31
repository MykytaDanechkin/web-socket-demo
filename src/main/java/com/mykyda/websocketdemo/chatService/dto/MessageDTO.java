package com.mykyda.websocketdemo.chatService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    private Long chatId;

    private String content;

    private Long sendersId;

    private String targetEmail;
}