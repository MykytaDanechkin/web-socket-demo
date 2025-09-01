package com.mykyda.websocketdemo.chatService.http.controller;

import com.mykyda.websocketdemo.chatService.dto.*;
import com.mykyda.websocketdemo.chatService.service.ChatFacadeService;
import com.mykyda.websocketdemo.chatService.service.ChatService;
import com.mykyda.websocketdemo.chatService.service.HistoryEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    private final HistoryEntryService historyEntryService;

    private final ChatFacadeService chatFacadeService;

    @GetMapping("/get-history")
    public List<HistoryEntryDTO> getLatestHistory(@RequestParam("chatId") Long chatId) {
        return historyEntryService.getLatestHistory(chatId);
    }

    @GetMapping("/get")
    public ChatDTO getChat(@RequestParam("id") Long chatId) {
        return chatService.getById(chatId);
    }

    @GetMapping("/get-full-history")
    public List<HistoryEntryDTO> getFullHistory(@RequestParam("chatId") Long chatId, @RequestParam("pageSize") Integer pageSize, @RequestParam("page") Integer page) {
        return historyEntryService.getEarlierHistory(chatId, pageSize, page);
    }

    @PostMapping("/create-with-message")
    public ChatDTO createChatWithMessage(@RequestBody ChatCreateDTO chatCreateDTO) {
        return chatFacadeService.createChatWithMessage(chatCreateDTO);
    }

    @PostMapping("/mark-seen")
    public ResponseEntity<Object> markSeen(@RequestBody MarkSeenRequestDTO markSeenRequestDTO) {
        chatFacadeService.markAsSeen(markSeenRequestDTO);
        return ResponseEntity.noContent().build();
    }

    @MessageMapping("/i")
    public void chatting(MessageDTO messageDTO) {
        chatFacadeService.handleMessage(messageDTO);
    }
}
