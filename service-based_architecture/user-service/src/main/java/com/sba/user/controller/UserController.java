package com.sba.user.controller;

import com.sba.common.dto.UserSummary;
import com.sba.user.dto.AuthRequest;
import com.sba.user.dto.AuthResponse;
import com.sba.user.dto.RegisterRequest;
import com.sba.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserSummary> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserSummary>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }
}
