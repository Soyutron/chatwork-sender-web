package com.example.chatwork.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * ============================================================
 * Setting
 * 機能: システム全体の設定（Chatworkトークンなど）を保持
 * ============================================================
 */
@Entity
@Data
@Table(name = "settings")
public class Setting {

    /** 固定ID（常に1件だけ存在） */
    @Id
    private Long id = 1L;

    /** Chatwork API トークン */
    @Column(name = "chatwork_token", length = 255)
    private String chatworkToken;
}
