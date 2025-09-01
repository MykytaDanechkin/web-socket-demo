package com.mykyda.websocketdemo.security.service;


import com.mykyda.websocketdemo.chatService.exception.DatabaseException;
import com.mykyda.websocketdemo.security.database.entity.User;
import com.mykyda.websocketdemo.security.database.repository.UserRepository;
import com.mykyda.websocketdemo.security.dto.UserDTO;
import com.mykyda.websocketdemo.security.exception.EmailUsedException;
import com.mykyda.websocketdemo.security.exception.TagUsedException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Failed to retrieve user " + username));
    }

    @Transactional
    public void checkByEmail(String email) {
        try {
            userRepository.findByEmail(email).orElseThrow(() -> {
                log.warn("user with email {} not found", email);
                return new EmailUsedException("email already in use");
            });
            log.info("user with email {} found", email);
        } catch (DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Transactional
    public void checkByTag(String tag) {
        try {
            userRepository.findByTag(tag).orElseThrow(() -> {
                log.warn("user with tag {} not found", tag);
                return new TagUsedException("tag already in use");
            });
            log.info("user with tag {} found", tag);
        } catch (DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Transactional
    public User findByEmail(String email) {
        try {
            var user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                log.warn("user with email {} not found", email);
            } else {
                log.info("user with email {} found", email);
            }
            return user;
        } catch (DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Transactional
    public User findByEmailUnlogged(String email) {
        try {
            return userRepository.findByEmail(email).orElse(null);
        } catch (DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Transactional
    public void save(User userToSave) {
        try {
            userRepository.save(userToSave);
            log.info("user with email {} saved with id {}", userToSave.getEmail(), userToSave.getId());
        } catch (DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Transactional
    public List<UserDTO> getAllContainsTag(String tag, String usersEmail) {
        try {
            var usersWithSimilarTag = userRepository.findByTagContains(tag).stream()
                    .filter(u -> !Objects.equals(u.getEmail(), usersEmail))
                    .map(UserDTO::of)
                    .toList();
            log.info("usersWithSimilarTag found: {}", usersWithSimilarTag);
            return usersWithSimilarTag;
        } catch (DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }

    @Transactional
    public void updateDisplayName(String email, String newDisplayName) {
        try {
            var user = userRepository.findByEmail(email).orElseThrow(() -> {
                log.warn("user with email {} not found", email);
                return new UsernameNotFoundException("Failed to retrieve user " + email);
            });
            log.info("user with email {} acquired for displaying name change", email);
            user.setDisplayName(newDisplayName);
            userRepository.save(user);
            log.info("user with email {} display name successfully updated", email);
        } catch (DataAccessException e) {
            throw new DatabaseException(e.getMessage());
        }
    }
}
