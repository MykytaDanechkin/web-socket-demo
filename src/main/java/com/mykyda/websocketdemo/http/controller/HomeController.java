package com.mykyda.websocketdemo.http.controller;


import com.mykyda.websocketdemo.dto.UserDto;
import com.mykyda.websocketdemo.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserService userService;

    @GetMapping
    public String index(Model model, Principal principal) {
        model.addAttribute("users", userService.getAll().stream().map(UserDto::of).toList());
        model.addAttribute("userEmail", principal.getName());
        return "index";
    }
}
