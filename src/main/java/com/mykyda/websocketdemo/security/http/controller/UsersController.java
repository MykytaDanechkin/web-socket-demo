package com.mykyda.websocketdemo.security.http.controller;

import com.mykyda.websocketdemo.security.dto.UserDTO;
import com.mykyda.websocketdemo.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;

    @GetMapping("/find")
    public List<UserDTO> findAllByTag(@RequestParam("tag") String tag, Principal principal) {
        return userService.getAllContainsTag(tag, principal.getName());
    }

    @PostMapping("/display-name")
    public ResponseEntity<Void> updateDisplayName(@RequestBody String newDisplayName, Principal principal) {
        userService.updateDisplayName(principal.getName(), newDisplayName);
        return ResponseEntity.noContent().build();
    }
}
