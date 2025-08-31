package com.mykyda.websocketdemo.security.service;


import com.mykyda.websocketdemo.security.dto.UserDTO;
import com.mykyda.websocketdemo.security.database.entity.User;
import com.mykyda.websocketdemo.security.database.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("Failed to retrieve user " + username));
    }

    @Transactional
    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Transactional
    public User save(User userToSave) {
        return userRepository.save(userToSave);
    }

    @Transactional
    public List<UserDTO> getAllContainsTag (String tag, String usersEmail) {
        return userRepository.findByTagContains(tag).stream()
                .filter(u -> !Objects.equals(u.getEmail(), usersEmail))
                .map(UserDTO::of)
                .toList();
    }

    @Transactional
    public User updateDisplayName(String email, String newDisplayName) {
        var user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Failed to retrieve user " + email));
        user.setDisplayName(newDisplayName);
        return userRepository.save(user);
    }
}
