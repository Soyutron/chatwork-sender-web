package com.example.chatwork.controller;

import com.example.chatwork.service.SettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================
 * TokenController
 * 機能: Chatwork API トークンの取得・更新
 * ============================================================
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/token")
public class TokenController {

    private final SettingService settingService;

    /**
     * 現在のトークンを取得（全ユーザーが閲覧可能）
     */
    @GetMapping
    public ResponseEntity<String> getToken() {
        String token = settingService.getChatworkToken();
        return ResponseEntity.ok(token == null ? "" : token);
    }

    /**
     * Chatworkトークンを更新（管理者のみ想定）
     */
    @PostMapping
    public ResponseEntity<String> updateToken(@RequestBody String newToken, Authentication auth) {
        // TODO: 管理者権限チェックを入れてもOK
        settingService.updateChatworkToken(newToken);
        return ResponseEntity.ok("✅ Chatworkトークンを更新しました。");
    }
}
