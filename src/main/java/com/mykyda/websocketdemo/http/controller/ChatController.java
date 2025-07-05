package com.mykyda.websocketdemo.http.controller;

import com.mykyda.websocketdemo.database.entity.MessageDTO;
import com.mykyda.websocketdemo.service.ChatService;
import com.mykyda.websocketdemo.service.HistoryEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    private final SimpMessagingTemplate messagingTemplate;

    private final HistoryEntryService  historyEntryService;

    @PostMapping("/check-chat")
    public ResponseEntity<Long> checkChat(Principal principal, @RequestParam("userId") Long user2Id) {
        return chatService.getChat(principal, user2Id);
    }

    //TODO validate
    @MessageMapping("/i")
    public void chatting(MessageDTO messageDTO, Principal principal) {
        log.info("Received message: {} from {} at chat {}", messageDTO.getContent(), principal.getName() , messageDTO.getChatId());
        historyEntryService.save(messageDTO, principal);
        messagingTemplate.convertAndSend("/topic/chat/" + messageDTO.getChatId(), messageDTO);
    }
}
