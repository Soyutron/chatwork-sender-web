package com.example.chatwork.controller;

import com.example.chatwork.entity.SendHistory;
import com.example.chatwork.service.SendHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/history")
public class SendHistoryController {

    private final SendHistoryService historyService;

    /** 🕓 履歴取得（既存） */
    @GetMapping
    public List<SendHistory> getUserHistory(Authentication auth) {
        return historyService.findByUser(auth.getName());
    }

}
