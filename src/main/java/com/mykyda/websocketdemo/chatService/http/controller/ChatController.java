package com.mykyda.websocketdemo.chatService.http.controller;

import com.mykyda.websocketdemo.chatService.dto.*;
import com.mykyda.websocketdemo.chatService.service.ChatService;
import com.mykyda.websocketdemo.chatService.service.HistoryEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    private final SimpMessagingTemplate messagingTemplate;

    private final HistoryEntryService historyEntryService;

    //todo handle
    @GetMapping("/get-history")
    public ResponseEntity<List<HistoryEntryDto>> getLatestHistory(@RequestParam("chatId") Long chatId) {
        var messages = historyEntryService.getLatestHistory(chatId);
        log.info("latest history acquired for chat {}", chatId);
        return ResponseEntity.ok(messages);
    }

    //TODO handle
    @GetMapping("/get")
    public ResponseEntity<ChatDTO> getChat(@RequestParam("id") Long chatId) {
        return ResponseEntity.ok(chatService.getById(chatId));
    }

    @GetMapping("/get-full-history")
    public ResponseEntity<List<HistoryEntryDto>> getFullHistory(@RequestParam("chatId") Long chatId, @RequestParam("pageSize") Integer pageSize, @RequestParam("page") Integer page) {
        var messages = historyEntryService.getEarlierHistory(chatId, pageSize, page);
        log.info("history with page {} and size {} acquired for chat {}", page, pageSize, chatId);
        return ResponseEntity.ok(messages);
    }

    //TODO handle , optimize
    @PostMapping("/create-with-message")
    public ResponseEntity<ChatDTO> createChat(@RequestBody ChatCreateDTO chatCreateDTO) {
        var chat = chatService.createChat(chatCreateDTO.getCurrentUserId(), chatCreateDTO.getTargetUserId());
        var historyEntry = historyEntryService.save(new MessageDTO(chat.getId(), chatCreateDTO.getInitialMessage(), chatCreateDTO.getCurrentUserId(), chatCreateDTO.getTargetEmail()));
        chat.setLastMessage(historyEntry);
        chatService.update(chat);
        messagingTemplate.convertAndSendToUser(chatCreateDTO.getTargetEmail(), "/queue/inbox", historyEntry);
        return ResponseEntity.ok(chat);
    }

    //TODO handle
    @PostMapping("/mark-seen")
    public ResponseEntity markSeen(@RequestBody MarkSeenRequest markSeenRequest) {
        System.out.println(markSeenRequest.getMessageIds());
        historyEntryService.markAsSeen(markSeenRequest.getMessageIds());
        log.info("mark seen at ids {}", markSeenRequest.getMessageIds());
        messagingTemplate.convertAndSend("/topic/chat/" + markSeenRequest.getChatId(), markSeenRequest.getMessageIds());
        return ResponseEntity.ok().build();
    }


    //TODO validate, optimize
    @MessageMapping("/i")
    public void chatting(MessageDTO messageDTO, Principal principal) {
        var message = historyEntryService.save(messageDTO);
        if (message != null) {

            log.info("Received message: {} from {} at chat {}", messageDTO.getContent(), principal.getName(), messageDTO.getChatId());

            var chat = chatService.getById(messageDTO.getChatId());
            chat.setLastMessage(HistoryEntryDto.builder().id(message.getId()).build());
            chatService.update(chat);

            messagingTemplate.convertAndSend("/topic/chat/" + messageDTO.getChatId(), message);
            messagingTemplate.convertAndSendToUser(messageDTO.getTargetEmail(), "/queue/inbox", message);
        }
    }


}
