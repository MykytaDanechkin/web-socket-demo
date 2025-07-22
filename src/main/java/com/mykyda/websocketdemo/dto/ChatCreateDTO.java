package com.mykyda.websocketdemo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatCreateDTO {

    private Long targetUserId;

    private Long currentUserId;
}
