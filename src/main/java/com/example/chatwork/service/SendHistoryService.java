package com.example.chatwork.service;

import com.example.chatwork.entity.SendHistory;
import com.example.chatwork.entity.User;
import com.example.chatwork.repository.SendHistoryRepository;
import com.example.chatwork.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SendHistoryService {

    private final SendHistoryRepository historyRepo;
    private final UserRepository userRepo;

    public void saveHistory(String username, Long roomId, String roomName, String message, String fileName) {
        User user = userRepo.findByUsername(username).orElseThrow();
        SendHistory h = new SendHistory();
        h.setUser(user);
        h.setRoomId(roomId);
        h.setRoomName(roomName);
        h.setMessage(message);
        h.setFileName(fileName);
        historyRepo.save(h);
    }

    public List<SendHistory> findByUser(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        return historyRepo.findByUserOrderBySentAtDesc(user);
    }
}
