package com.mykyda.websocketdemo.chatService.service;

import com.mykyda.websocketdemo.chatService.dto.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatFacadeService {

    private final ChatService chatService;

    private final HistoryEntryService historyEntryService;

    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatDTO createChatWithMessage(ChatCreateDTO chatCreateDTO) {
        var chat = chatService.createAndSaveChat(chatCreateDTO.getCurrentUserId(), chatCreateDTO.getTargetUserId());

        var firstMessage = historyEntryService.save(
                new MessageDTO(chat.getId()
                        ,chatCreateDTO.getInitialMessage()
                        ,chatCreateDTO.getCurrentUserId()
                        ,chatCreateDTO.getTargetEmail()));
        chat.setLastMessage(firstMessage);
        chatService.updateLastMessage(chat);

        log.info("Chat successfully created with id {} and first message saved with id {} and inserted in chat entity ", chat.getId(), firstMessage.getId());

        messagingTemplate.convertAndSendToUser(chatCreateDTO.getTargetEmail(), "/queue/inbox", firstMessage);
        return chat;
    }

    @Transactional
    public void markAsSeen(MarkSeenRequestDTO markSeenRequestDTO) {
        historyEntryService.markAsSeen(markSeenRequestDTO.getMessageIds());
        log.info("markAsSeen successful: {}", markSeenRequestDTO.getMessageIds());
        messagingTemplate.convertAndSend("/topic/chat/" + markSeenRequestDTO.getChatId(), markSeenRequestDTO.getMessageIds());
    }

    @Transactional
    public void handleMessage(MessageDTO messageDTO) {
        var message = historyEntryService.save(messageDTO);
        log.info("Received message: {} from user with id {} at chat {}", messageDTO.getContent(), message.getSenderId(), messageDTO.getChatId());

        var chat = chatService.getById(messageDTO.getChatId());
        chat.setLastMessage(HistoryEntryDTO.builder().id(message.getId()).build());
        chatService.updateLastMessage(chat);

        log.info("Message saved and chat last message successfully updated at chat {}", messageDTO.getChatId());

        messagingTemplate.convertAndSend("/topic/chat/" + messageDTO.getChatId(), message);
        messagingTemplate.convertAndSendToUser(messageDTO.getTargetEmail(), "/queue/inbox", message);

    }
}