package com.mykyda.websocketdemo.chatService.service;

import com.mykyda.websocketdemo.chatService.database.entity.Chat;
import com.mykyda.websocketdemo.chatService.database.entity.HistoryEntry;
import com.mykyda.websocketdemo.chatService.database.entity.MessageStatus;
import com.mykyda.websocketdemo.chatService.database.repository.HistoryEntryRepository;
import com.mykyda.websocketdemo.chatService.dto.HistoryEntryDTO;
import com.mykyda.websocketdemo.chatService.dto.MessageDTO;
import com.mykyda.websocketdemo.chatService.exception.DatabaseException;
import com.mykyda.websocketdemo.chatService.exception.NotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class HistoryEntryService {

    private final HistoryEntryRepository historyEntryRepository;


    private final EntityManager entityManager;

    @Value("${chat.init-history-size:20}")
    private Integer LATEST_HISTORY_SIZE;

    @Transactional
    public HistoryEntryDTO save(MessageDTO messageDTO) {
        var historyEntry = HistoryEntry.builder()
                .chat(entityManager.find(Chat.class, messageDTO.getChatId()))
                .content(StringEscapeUtils.escapeHtml4(messageDTO.getContent()))
                .sendersId(messageDTO.getSendersId())
                .build();
        try {
            var saved = historyEntryRepository.save(historyEntry);
            log.info("Saving history entry: {}", historyEntry);
            return HistoryEntryDTO.of(saved);
        } catch (DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Transactional
    public List<HistoryEntryDTO> getLatestHistory(Long chatId) {
        try {
            var latestHistoryList = Stream.concat(
                            historyEntryRepository.findAllByChatIdAndStatusOrderByTimestampDesc(chatId, MessageStatus.UNSEEN)
                                    .stream(),
                            historyEntryRepository.findAllByChatIdAndStatusNotOrderByTimestampDesc(chatId, MessageStatus.UNSEEN, PageRequest.of(0, LATEST_HISTORY_SIZE))
                                    .stream())
                    .map(HistoryEntryDTO::of)
                    .toList();
            log.info("latest history acquired for chat {}", chatId);
            return latestHistoryList;
        } catch (DataAccessException exception) {
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Transactional
    public List<HistoryEntryDTO> getEarlierHistory(Long chatId, Integer pageSize, Integer page) {
        try {
            var earlierHistoryList = historyEntryRepository.findAllByChatIdOrderByTimestampDesc(chatId, PageRequest.of(page, pageSize))
                    .stream()
                    .map(HistoryEntryDTO::of)
                    .toList();
            log.info("history with page {} and size {} acquired for chat {}", page, pageSize, chatId);
            return earlierHistoryList;
        } catch (DataAccessException exception) {
            throw new DatabaseException(exception.getMessage());
        }
    }

    @Transactional
    public void markAsSeen(List<Long> messageIds) {
        try {
            var messagesToChange = historyEntryRepository.findAllById(messageIds);
            if (!messagesToChange.isEmpty()) {
                historyEntryRepository.updateStatusByIds(messageIds, MessageStatus.SEEN);
                log.info("mark seen at ids {}", messageIds);
            } else {
                log.warn("mark seen at ids {} failed", messageIds);
                throw new NotFoundException("mark seen at ids " + messageIds);
            }
        } catch (DataAccessException exception) {
            throw new DatabaseException(exception.getMessage());
        }
    }
}
