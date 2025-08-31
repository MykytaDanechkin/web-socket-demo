package com.mykyda.websocketdemo.chatService.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MarkSeenRequest {

    private List<Long> messageIds;

    private Long chatId;
}