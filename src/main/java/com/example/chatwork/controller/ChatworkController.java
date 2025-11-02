package com.example.chatwork.controller;

import com.example.chatwork.model.ChatworkRoom;
import com.example.chatwork.service.ChatworkService;
import com.example.chatwork.service.SendHistoryService;
import com.example.chatwork.service.SendStatusService;
import com.example.chatwork.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * ============================================================
 * ChatworkController
 * 機能: Chatworkルーム取得・送信（ユーザーごとのトークン使用）
 * ============================================================
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatwork")
public class ChatworkController {

    private final ChatworkService chatworkService;
    private final UserService userService;
    private final SendHistoryService historyService;
    private final SendStatusService statusService;
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * 💬 Chatworkルーム一覧取得
     */
    @GetMapping("/rooms")
    public List<ChatworkRoom> getRooms(Authentication auth) {
        String token = userService.getToken(auth.getName());
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Chatworkトークンが未設定です。");
        }
        return chatworkService.getRooms(token);
    }

    /**
     * 📤 メッセージまたはファイル送信（5秒間隔で送信）
     */
    @PostMapping(value = "/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> sendMessage(
            @RequestParam("message") String message,
            @RequestParam("roomIds") String roomIdsJson,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Authentication auth) throws IOException {

        String username = auth.getName();
        String token = userService.getToken(username);
        statusService.clearCancel(username);

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body("❌ Chatworkトークンが未設定です。");
        }

        List<Long> roomIds = mapper.readValue(roomIdsJson, new TypeReference<>() {});

        // ✅ tempFilePathを最初からfinalで宣言する
        final Path finalTempFilePath;
        if (file != null && !file.isEmpty()) {
            finalTempFilePath = Files.createTempFile("chatwork_upload_", "_" + file.getOriginalFilename());
            Files.write(finalTempFilePath, file.getBytes());
            log.info("📎 一時ファイル作成: {}", finalTempFilePath);
        } else {
            finalTempFilePath = null;
        }

        final String finalMessage = message;
        final String finalFileName = (file != null && !file.isEmpty()) ? file.getOriginalFilename() : null;
        final String finalUsername = username;
        final String finalToken = token;

        new Thread(() -> {
            try {
                statusService.updateStatus(finalUsername, "🚀 送信開始...");
                int total = roomIds.size();

                for (int i = 0; i < total; i++) {
                    if (statusService.isCancelled(finalUsername)) {
                        statusService.updateStatus(finalUsername, "⏹️ ユーザーによって中止されました");
                        log.info("⏹️ 中止要求を検知（roomId={} で停止）", roomIds.get(i));
                        break;
                    }

                    Long roomId = roomIds.get(i);
                    statusService.updateStatus(finalUsername, "送信中: " + (i + 1) + "/" + total);
                    log.info("🚀 送信開始: roomId={} message='{}'", roomId, finalMessage);

                    chatworkService.sendMessageOrFile(
                            finalToken,
                            roomId,
                            finalMessage,
                            finalTempFilePath != null ? finalTempFilePath.toString() : null
                    );

                    String roomName = chatworkService.getRoomName(roomId, finalToken);
                    historyService.saveHistory(finalUsername, roomId, roomName, finalMessage, finalFileName);

                    log.info("✅ 送信完了: room={} file={} messageLength={}",
                            roomName, finalFileName, finalMessage != null ? finalMessage.length() : 0);

                    if (i < total - 1) Thread.sleep(5000);
                }

                statusService.updateStatus(finalUsername, "✅ 全送信完了");

            } catch (Exception e) {
                statusService.updateStatus(finalUsername, "❌ エラー: " + e.getMessage());
                log.error("❌ 送信中にエラー: {}", e.getMessage(), e);
            } finally {
                if (finalTempFilePath != null) {
                    try {
                        Files.deleteIfExists(finalTempFilePath);
                        log.info("🧹 一時ファイル削除: {}", finalTempFilePath);
                    } catch (IOException ex) {
                        log.warn("⚠️ 一時ファイル削除失敗: {}", ex.getMessage());
                    }
                }
            }
        }).start();

        return ResponseEntity.ok("📡 送信ジョブを開始しました。");
    }


    /**
     * 📡 現在の送信状態を取得
     */
    @GetMapping("/status")
    public ResponseEntity<String> getStatus(Authentication auth) {
        String username = auth.getName();
        return ResponseEntity.ok(statusService.getStatus(username));
    }

    /**
     * 🛑 中止API
     */
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelSend(Authentication auth) {
        String username = auth.getName();
        statusService.requestCancel(username);
        return ResponseEntity.ok("⏹️ 送信を中止しました");
    }
}
