package com.mykyda.websocketdemo.security.http.controller;

import com.mykyda.websocketdemo.security.dto.LoginDto;
import com.mykyda.websocketdemo.security.dto.RegistrationDto;
import com.mykyda.websocketdemo.security.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;

@Controller
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginDto ld, Model model, HttpServletResponse response) {
        var res = authService.login(ld);

        switch (res.getStatusCode().value()) {
            case 401, 404 -> {
                model.addAttribute("errors", "incorrect username or password");
                return "login";
            }
            case 200 -> {
                response.addCookie(res.getBody());
                return "redirect:/";
            }
            default -> {
                model.addAttribute("errors", "Unexpected error");
                return "login";
            }
        }
    }

    @GetMapping("/registration")
    public String regPage() {
        return "registration";
    }

    @PostMapping("/registration")
    public String register(@ModelAttribute RegistrationDto rd, Model model) {
        var res = authService.register(rd);
        if (res.getStatusCode() != HttpStatus.CREATED) {
            model.addAttribute("errors", Collections.singletonList(res.getBody()));
            return "registration";
        }
        return "redirect:/auth/login";
    }
}
