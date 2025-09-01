package com.mykyda.websocketdemo.security.http.controller;

import com.mykyda.websocketdemo.security.dto.LoginDTO;
import com.mykyda.websocketdemo.security.dto.RegistrationDTO;
import com.mykyda.websocketdemo.security.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

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
    public String login(@ModelAttribute LoginDTO ld, Model model, HttpServletResponse response) {
        try {
            var cookie = authService.login(ld);
            response.addCookie(cookie);
            return "redirect:/";
        } catch (Exception ex) {
            model.addAttribute("errors", List.of(ex.getMessage()));
            return "login";
        }
    }

    @GetMapping("/registration")
    public String regPage() {
        return "registration";
    }

    @PostMapping("/registration")
    public String register(@ModelAttribute RegistrationDTO rd, Model model) {
        try {
            authService.register(rd);
            return "redirect:/auth/login";
        } catch (Exception ex) {
            model.addAttribute("errors", List.of(ex.getMessage()));
            return "registration";
        }
    }
}
