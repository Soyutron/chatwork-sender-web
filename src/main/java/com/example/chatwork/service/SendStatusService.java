package com.example.chatwork.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class SendStatusService {

    private final Map<String, String> userStatus = new ConcurrentHashMap<>();
    private final Map<String, Boolean> cancelFlags = new ConcurrentHashMap<>();

    public void updateStatus(String username, String status) {
        userStatus.put(username, status);
    }

    public String getStatus(String username) {
        return userStatus.getOrDefault(username, "待機中");
    }

    public void requestCancel(String username) {
        cancelFlags.put(username, true);
        updateStatus(username, "⏹️ 中止リクエスト受信");
    }

    public boolean isCancelled(String username) {
        return cancelFlags.getOrDefault(username, false);
    }

    public void clearCancel(String username) {
        cancelFlags.remove(username);
    }
}
