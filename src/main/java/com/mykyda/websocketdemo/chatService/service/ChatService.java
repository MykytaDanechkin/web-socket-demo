package com.mykyda.websocketdemo.chatService.service;

import com.mykyda.websocketdemo.chatService.database.entity.Chat;
import com.mykyda.websocketdemo.chatService.database.entity.HistoryEntry;
import com.mykyda.websocketdemo.chatService.database.repository.ChatRepository;
import com.mykyda.websocketdemo.chatService.dto.ChatDTO;
import com.mykyda.websocketdemo.chatService.exception.DatabaseException;
import com.mykyda.websocketdemo.chatService.exception.NotFoundException;
import com.mykyda.websocketdemo.security.database.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatRepository chatRepository;

    private final EntityManager entityManager;

    @Transactional
    public ChatDTO getById(Long id) {
        try {
            var chat = chatRepository.findById(id).map(ChatDTO::of).orElseThrow(() -> {
                log.warn("chat with id {} not found", id);
                return new NotFoundException("chat with id" + id + " not found");
            });
            log.info("chat with id {} found", chat.getId());
            return chat;
        } catch (DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Transactional
    public void updateLastMessage(ChatDTO chatDTO) {
        try {
            chatRepository.findById(chatDTO.getId()).orElseThrow(()-> {
                log.warn("chat with id {} not found", chatDTO.getId());
                return new NotFoundException("chat with id" + chatDTO.getId() + " not found");
            });
            chatRepository.updateLastMessage(chatDTO.getId(), entityManager.getReference(HistoryEntry.class, chatDTO.getLastMessage().getId()));
            log.info("chat with id {} last message updated ", chatDTO.getId());
        } catch (DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Transactional
    public List<ChatDTO> getAllForUserId(Long userId) {
        try {
            var availableChats = chatRepository.findAllByUser1IdOrUser2IdOrderByLastMessageTimestampDesc(userId, userId).stream().map(ChatDTO::of).toList();
            log.info("found available chats for user {}", userId);
            return availableChats;
        } catch (DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Transactional
    public ChatDTO createAndSaveChat(Long currentUserId, Long targetUserId) {
        var chat = Chat.of(entityManager.getReference(User.class, currentUserId), entityManager.getReference(User.class, targetUserId));
        try {
            var savedChat = chatRepository.save(chat);
            log.info("chat with id {} saved", savedChat.getId());
            return ChatDTO.of(savedChat);
        }  catch (DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }

    }
}
