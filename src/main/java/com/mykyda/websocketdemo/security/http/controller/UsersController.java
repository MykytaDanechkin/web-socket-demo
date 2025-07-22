package com.mykyda.websocketdemo.security.http.controller;

import com.mykyda.websocketdemo.security.dto.UserDTO;
import com.mykyda.websocketdemo.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;

    //TODO handle, log
    @GetMapping("/find")
    public ResponseEntity<List<UserDTO>> findAllByEmail(@RequestParam("email") String email, Principal principal) {
        return new ResponseEntity<>(userService.getAllContainsEmail(email, principal.getName()), HttpStatus.OK);
    }
}
