package com.example.chatwork.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "send_history")
public class SendHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String roomName;
    private Long roomId;
    private String message;
    private String fileName;
    private LocalDateTime sentAt = LocalDateTime.now();
}
