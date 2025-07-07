package com.mykyda.websocketdemo.http.controller;

import com.mykyda.websocketdemo.database.entity.MessageDTO;
import com.mykyda.websocketdemo.dto.HistoryEntryDto;
import com.mykyda.websocketdemo.service.ChatService;
import com.mykyda.websocketdemo.service.HistoryEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/api/chat")
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

    @GetMapping("/get-history")
    public ResponseEntity<List<HistoryEntryDto>> getLatestHistory(@RequestParam("chatId") Long chatId) {
        var messages = historyEntryService.getLatestHistory(chatId);
        log.info("latest history acquired for chat {}", chatId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/get-full-history")
    public ResponseEntity<List<HistoryEntryDto>> getFullHistory(@RequestParam("chatId") Long chatId) {
        var messages = historyEntryService.getFullHistory(chatId);
        log.info("full history acquired for chat {}", chatId);
        return ResponseEntity.ok(messages);
    }

    //TODO validate
    @MessageMapping("/i")
    public void chatting(MessageDTO messageDTO, Principal principal) {
        var message = historyEntryService.save(messageDTO, principal);
        log.info("Received message: {} from {} at chat {}", messageDTO.getContent(), principal.getName() , messageDTO.getChatId());
        messagingTemplate.convertAndSend("/topic/chat/" + messageDTO.getChatId(), message.getBody());
    }
}
