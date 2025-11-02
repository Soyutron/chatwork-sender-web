package com.example.chatwork.model;

import lombok.Data;
import java.util.List;

/**
 * ============================================================
 * SendRequest
 * 機能: 一斉送信用のリクエストデータ（JSONバインド用）
 * ============================================================
 */
@Data
public class SendRequest {

    /** 送信先のルームID一覧 */
    private List<Long> roomIds;

    /** 送信メッセージ本文 */
    private String message;

    /** 送信ファイルのパス（任意） */
    private String filePath;
}
