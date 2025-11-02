package com.example.chatwork.controller;

import com.example.chatwork.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    // ✅ トークン保存
    @PostMapping("/token")
    public void saveToken(@RequestBody String token, Authentication auth) {
        userService.saveToken(auth.getName(), token.trim());
    }

    // ✅ トークン取得（JSON形式で返却）
    @GetMapping("/token")
    public TokenResponse getToken(Authentication auth) {
        String token = userService.getToken(auth.getName());
        return new TokenResponse(token);
    }

    // ✅ レスポンス用DTO（Java 16+のrecord構文を使用）
    public record TokenResponse(String token) {}
}
