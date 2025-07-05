package com.mykyda.websocketdemo.http.controller;

import com.mykyda.websocketdemo.database.entity.MessageDTO;
import com.mykyda.websocketdemo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Objects;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/check-chat")
    public ResponseEntity<Long> checkChat(Principal principal, @RequestParam("userId") Long user2Id) {
        return chatService.getChat(principal, user2Id);
    }

    //TODO validate
    @MessageMapping("/i")
    public void greeting(MessageDTO messageDTO, Principal principal) {
        System.out.println("Received message: " + messageDTO.getContent() + "from " + principal.getName());
        messagingTemplate.convertAndSend("/topic/chat/" + messageDTO.getChatId(), messageDTO);
    }
}
