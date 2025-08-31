package com.mykyda.websocketdemo.chatService.service;

import com.mykyda.websocketdemo.chatService.database.entity.Chat;
import com.mykyda.websocketdemo.chatService.database.entity.HistoryEntry;
import com.mykyda.websocketdemo.chatService.database.entity.MessageStatus;
import com.mykyda.websocketdemo.chatService.database.repository.HistoryEntryRepository;
import com.mykyda.websocketdemo.chatService.dto.HistoryEntryDto;
import com.mykyda.websocketdemo.chatService.dto.MessageDTO;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryEntryService {

    private final HistoryEntryRepository historyEntryRepository;

    private final ChatService chatService;

    private final EntityManager entityManager;

    @Transactional
    public HistoryEntryDto save(MessageDTO messageDTO) {
        var chat = chatService.getById(messageDTO.getChatId());
        if (chat == null) {
            log.warn("Chat id not found: {}", messageDTO.getChatId());
            return null;
        } else {
            var historyEntry = HistoryEntry.builder()
                    .chat(entityManager.find(Chat.class, chat.getId()))
                    .content(StringEscapeUtils.escapeHtml4(messageDTO.getContent()))
                    .sendersId(messageDTO.getSendersId())
                    .build();
            log.info("Saving history entry: {}", historyEntry);
            var saved = historyEntryRepository.save(historyEntry);
            return HistoryEntryDto.of(saved);
        }
    }

    //TODO handle errors
    @Transactional
    public List<HistoryEntryDto> getLatestHistory(Long chatId) {
        return Stream.concat(
                        historyEntryRepository.findAllByChatIdAndStatusOrderByTimestampDesc(chatId,
                                        MessageStatus.UNSEEN).
                                stream().map(HistoryEntryDto::of),
                        historyEntryRepository.findAllByChatIdAndStatusNotOrderByTimestampDesc(chatId,
                                        MessageStatus.UNSEEN,
                                        PageRequest.of(0, 20))
                                .stream().map(HistoryEntryDto::of))
                .toList();
    }

    //TOdo handle
    @Transactional
    public List<HistoryEntryDto> getEarlierHistory(Long chatId, Integer pageSize,  Integer page) {
        return historyEntryRepository.findAllByChatIdOrderByTimestampDesc(chatId,PageRequest.of(page,pageSize))
                .stream()
                .map(HistoryEntryDto::of)
                .toList();
    }

    //TODO handle errors
    @Transactional
    public void markAsSeen(List<Long> messageIds) {
        historyEntryRepository.updateStatusByIds(messageIds, MessageStatus.SEEN);
    }
}
