package com.sba.user.service;

import com.sba.common.dto.UserSummary;
import com.sba.common.enums.Role;
import com.sba.security.JwtService;
import com.sba.user.dto.AuthRequest;
import com.sba.user.dto.AuthResponse;
import com.sba.user.dto.RegisterRequest;
import com.sba.user.entity.AppUser;
import com.sba.user.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public UserSummary register(RegisterRequest request) {
        appUserRepository.findByUsername(request.username()).ifPresent(existing -> {
            throw new IllegalArgumentException("Username already exists");
        });

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role() == null ? Role.USER : request.role());
        AppUser saved = appUserRepository.save(user);
        return new UserSummary(saved.getId(), saved.getUsername(), saved.getRole().name());
    }

    public AuthResponse login(AuthRequest request) {
        AppUser user = appUserRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getId(), user.getUsername(), user.getRole().name());
    }

    public List<UserSummary> getUsers() {
        return appUserRepository.findAll().stream()
                .map(u -> new UserSummary(u.getId(), u.getUsername(), u.getRole().name()))
                .toList();
    }
}
