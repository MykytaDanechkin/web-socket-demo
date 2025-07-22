package com.mykyda.websocketdemo.advice;

import com.mykyda.websocketdemo.dto.ChatDTO;
import com.mykyda.websocketdemo.security.service.UserService;
import com.mykyda.websocketdemo.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final ChatService chatService;

    private final UserService userService;

    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    @ModelAttribute("userEmail")
    public String userEmail(Authentication authentication) {
        return (authentication != null) ? authentication.getName() : null;
    }

    @ModelAttribute("userId")
    public Long userId(Authentication authentication) {
        return (authentication != null) ? userService.getByEmail(authentication.getName()).getId() : null;
    }

    @ModelAttribute("chats")
    public List<ChatDTO> chats(Authentication authentication) {
        return (authentication != null) ? chatService.getAllForUserId(userId(authentication)) : null;
    }
}
