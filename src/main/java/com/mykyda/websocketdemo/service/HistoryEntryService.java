package com.mykyda.websocketdemo.service;

import com.mykyda.websocketdemo.database.entity.HistoryEntry;
import com.mykyda.websocketdemo.database.entity.MessageDTO;
import com.mykyda.websocketdemo.database.repository.HistoryEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryEntryService {

    private final HistoryEntryRepository historyEntryRepository;

    private final ChatService chatService;

    public ResponseEntity<HistoryEntry> save(MessageDTO messageDTO, Principal principal) {
        var chat = chatService.getById(messageDTO.getChatId()).getBody();
        if (chat == null) {
            log.warn("Chat id not found: {}", messageDTO.getChatId());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            var historyEntry = HistoryEntry.builder()
                    .chatId(chat)
                    .content(messageDTO.getContent())
                    .sendersEmail(principal.getName())
                    .build();
            log.info("Saving history entry: {}", historyEntry);
            return ResponseEntity.ok(historyEntryRepository.save(historyEntry));
        }
    }
}
