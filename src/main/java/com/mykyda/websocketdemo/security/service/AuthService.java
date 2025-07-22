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
        var user = userService.getByEmail(loginDto.getEmail());
        if (user != null) {
            try {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
                log.info("Authentication Successful in {}", loginDto.getEmail());
                return ResponseEntity.ok(jwtService.generateCookie(user));
            } catch (BadCredentialsException e) {
                log.warn("Bad credentials for email {}", loginDto.getEmail());
                return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
            }
        } else {
            log.warn("Invalid email {}", loginDto.getEmail());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public ResponseEntity<String> register(RegistrationDto rd) {
        if (userService.getByEmail(rd.getEmail()) == null) {
            var user = User.builder()
                    .email(rd.getEmail())
                    .password(passwordEncoder.encode(rd.getPassword()))
                    .role(Role.USER)
                    .build();
            System.out.println(rd.getPassword());
            userService.save(user);
            log.info("Registration Successful in {}", rd.getEmail());
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            log.warn("User {} already exists", rd.getEmail());
            return new ResponseEntity<>("email already in use", HttpStatus.CONFLICT);
        }
    }
}
