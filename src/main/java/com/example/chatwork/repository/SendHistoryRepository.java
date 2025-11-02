package com.example.chatwork.repository;

import com.example.chatwork.entity.SendHistory;
import com.example.chatwork.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SendHistoryRepository extends JpaRepository<SendHistory, Long> {
    List<SendHistory> findByUserOrderBySentAtDesc(User user);
}
