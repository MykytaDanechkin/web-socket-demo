package com.mykyda.websocketdemo.security.service;


import com.mykyda.websocketdemo.security.database.entity.Role;
import com.mykyda.websocketdemo.security.database.entity.User;
import com.mykyda.websocketdemo.security.dto.LoginDto;
import com.mykyda.websocketdemo.security.dto.RegistrationDto;
import jakarta.servlet.http.Cookie;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Cookie> login(LoginDto loginDto) {
        var user = userService.getByEmail(loginDto.email());
        if (user != null) {
            try {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password()));
                log.info("Authentication Successful in {}", loginDto.email());
                return ResponseEntity.ok(jwtService.generateCookie(user));
            } catch (BadCredentialsException e) {
                log.warn("Bad credentials for email {}", loginDto.email());
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }
        } else {
            log.warn("Invalid email {}", loginDto.email());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public ResponseEntity<String> register(RegistrationDto rd) {
        if (userService.getByEmail(rd.email()) == null) {
            var user = User.builder()
                    .email(rd.email())
                    .tag(rd.tag())
                    .password(passwordEncoder.encode(rd.password()))
                    .role(Role.USER)
                    .build();
            if (!rd.displayName().isEmpty()){
                user.setDisplayName(rd.displayName());
            }
            userService.save(user);
            log.info("Registration Successful in {}", rd.email());
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            log.warn("User {} already exists", rd.email());
            return new ResponseEntity<>("email already in use", HttpStatus.CONFLICT);
        }
    }
}
