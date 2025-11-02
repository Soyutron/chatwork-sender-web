package com.example.chatwork.service;

import com.example.chatwork.entity.Setting;
import com.example.chatwork.repository.SettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final SettingRepository repo;

    /**
     * Chatworkトークンを取得
     */
    public String getChatworkToken() {
        return repo.findById(1L)
                .map(Setting::getChatworkToken)
                .orElse(null);
    }

    /**
     * Chatworkトークンを更新（存在しない場合は新規作成）
     */
    public void updateChatworkToken(String token) {
        Setting setting = repo.findById(1L).orElse(new Setting());
        setting.setId(1L);
        setting.setChatworkToken(token);
        repo.save(setting);
    }
}
