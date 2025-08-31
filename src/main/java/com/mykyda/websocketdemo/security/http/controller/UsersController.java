package com.mykyda.websocketdemo.security.http.controller;

import com.mykyda.websocketdemo.security.dto.UserDTO;
import com.mykyda.websocketdemo.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;

    //TODO handle, log
    @GetMapping("/find")
    public ResponseEntity<List<UserDTO>> findAllByTag(@RequestParam("tag") String tag, Principal principal) {
        return new ResponseEntity<>(userService.getAllContainsTag(tag, principal.getName()), HttpStatus.OK);
    }

    @PostMapping("/display-name")
    public ResponseEntity<Void> updateDisplayName(@RequestBody String newDisplayName, Principal principal) {
        userService.updateDisplayName(principal.getName(), newDisplayName);
        return ResponseEntity.ok().build();
    }
}
