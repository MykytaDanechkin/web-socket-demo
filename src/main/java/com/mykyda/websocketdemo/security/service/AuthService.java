package com.mykyda.websocketdemo.security.service;


import com.mykyda.websocketdemo.security.database.entity.Role;
import com.mykyda.websocketdemo.security.database.entity.User;
import com.mykyda.websocketdemo.security.dto.LoginDTO;
import com.mykyda.websocketdemo.security.dto.RegistrationDTO;
import com.mykyda.websocketdemo.security.exception.UserNotFoundException;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    @Transactional
    public Cookie login(LoginDTO loginDto) {
            var user = userService.findByEmail(loginDto.email());
            if (user != null) {
                try {
                    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password()));
                    log.info("Authentication Successful in {}", loginDto.email());
                    return jwtService.generateCookie(user);
                } catch (BadCredentialsException e) {
                    log.warn("Bad credentials for email {}", loginDto.email());
                    throw new BadCredentialsException("Bad credentials");
                }
            } else {
                log.warn("Invalid email {}", loginDto.email());
                throw new UserNotFoundException("Invalid email");
            }

    }

    @Transactional
    public void register(RegistrationDTO rd) {
        userService.checkByEmail(rd.email());
        userService.checkByTag(rd.tag());
        var user = User.builder()
                .email(rd.email())
                .tag(rd.tag())
                .password(passwordEncoder.encode(rd.password()))
                .role(Role.USER)
                .build();
        if (!rd.displayName().isEmpty()) {
            user.setDisplayName(rd.displayName());
        }
        userService.save(user);
        log.info("Registration Successful in {}", rd.email());
    }
}
