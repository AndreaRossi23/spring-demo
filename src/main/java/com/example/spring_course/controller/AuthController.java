package com.example.spring_course.controller;

import com.example.spring_course.controller.model.RegisterRequest;
import com.example.spring_course.controller.model.UserResponse;
import com.example.spring_course.service.RegistrationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final org.springframework.security.authentication.AuthenticationManager authenticationManager;
    private final com.example.spring_course.service.TokenService tokenService;

    private final RegistrationService registrationService;

    public AuthController(RegistrationService registrationService,
            org.springframework.security.authentication.AuthenticationManager authenticationManager,
            com.example.spring_course.service.TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.registrationService = registrationService;
    }

    @PostMapping("/login")
    public ResponseEntity<com.example.spring_course.controller.model.TokenResponse> login(
            @Valid @RequestBody com.example.spring_course.controller.model.LoginRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                            .unauthenticated(request.username(), request.password()));
            return ResponseEntity.ok().header("Cache-Control", "no-store")
                    .body(tokenService.issue(authentication));
        } catch (org.springframework.security.core.AuthenticationException exception) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Credenziali non valide");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {

        UserResponse response = registrationService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}