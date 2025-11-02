package com.example.chatwork.config;

import com.example.chatwork.entity.User;
import com.example.chatwork.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * ============================================================
 * AdminUserInitializer
 * 機能: 初回起動時に admin ユーザーを自動登録
 * ============================================================
 */
@Configuration
@RequiredArgsConstructor
public class AdminUserInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner createDefaultAdminUser() {
        return args -> {
            String username = "anakae@jicpa-work.com";

            // すでに存在していればスキップ
            if (userRepository.findByUsername(username).isPresent()) {
                System.out.println("✅ admin ユーザーは既に存在しています。");
                return;
            }

            // 存在しない場合は作成
            User admin = new User();
            admin.setUsername(username);
            admin.setPassword(passwordEncoder.encode("GUkFUx73lURakbny7A7A")); // ← 初期パスワード
            admin.setChatworkToken(null);

            userRepository.save(admin);
            System.out.println("👑 初期管理者アカウントを作成しました: admin / admin123");
        };
    }
}
