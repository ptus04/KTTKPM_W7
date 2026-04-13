package com.sba.user.config;

import com.sba.common.enums.Role;
import com.sba.user.entity.AppUser;
import com.sba.user.repository.AppUserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        appUserRepository.findByUsername("admin").ifPresentOrElse(
                existing -> {
                },
                () -> {
                    AppUser admin = new AppUser();
                    admin.setUsername("admin");
                    admin.setPassword(passwordEncoder.encode("admin123"));
                    admin.setRole(Role.ADMIN);
                    appUserRepository.save(admin);
                }
        );
    }
}
