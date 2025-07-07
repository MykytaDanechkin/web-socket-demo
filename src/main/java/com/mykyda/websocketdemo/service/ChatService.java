package com.mykyda.websocketdemo.service;

import com.mykyda.websocketdemo.database.entity.Chat;
import com.mykyda.websocketdemo.database.repository.ChatRepository;
import com.mykyda.websocketdemo.security.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatRepository chatRepository;

    private final UserService userService;
    
    @Transactional
    public ResponseEntity<Long> getChat(Principal principal, Long user2Id) {
        var user1 = userService.findByEmail(principal.getName());
        var firstId = Math.min(user1.getId(), user2Id);
        var lastId = Math.max(user1.getId(), user2Id);
        var chat = chatRepository.findByUser1IdAndUser2Id(firstId, lastId).orElse(null);
        if (chat == null) {
            log.warn("Chat not found with ids {} {}", user1.getId(), user2Id);
            var newChat = createChat(user1.getId(), user2Id);
            if (newChat == null) {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(newChat.getId(), HttpStatus.OK);
            }

        } else {
            log.info("Chat found with ids {} {}", user1.getId(), user2Id);
            return new ResponseEntity<>(chat.getId(), HttpStatus.OK);
        }
    }

    @Transactional
    public ResponseEntity<Chat> getById(Long id) {
        var chat = chatRepository.findById(id).orElse(null);
        if (chat == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(chat, HttpStatus.OK);
        }
    }

    private Chat createChat(Long user1Id, Long user2Id) {
        var chatToSave = Chat.of(user1Id, user2Id);
        try {
            var savedChat = chatRepository.save(chatToSave);
            log.info("Created chat: {}", chatToSave);
            return savedChat;
        } catch (Exception e) {
            log.error("Unexpected error in Chat save with:{}", e.getMessage());
            return null;
        }
    }
}
