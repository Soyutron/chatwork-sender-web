package com.example.chatwork.service;

import com.example.chatwork.model.ChatworkRoom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class ChatworkService {

    private static final String CHATWORK_API_BASE = "https://api.chatwork.com/v2";
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 📡 Chatworkルーム一覧を取得（type=group のみ）
     */
    public List<ChatworkRoom> getRooms(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Chatwork token is not set.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-ChatWorkToken", token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<ChatworkRoom[]> response = restTemplate.exchange(
                CHATWORK_API_BASE + "/rooms",
                HttpMethod.GET,
                entity,
                ChatworkRoom[].class
        );

        ChatworkRoom[] rooms = response.getBody();
        if (rooms == null) return List.of();

        return Arrays.stream(rooms)
                .filter(r -> "group".equalsIgnoreCase(r.getType()))
                .toList();
    }

    /**
     * 📨 メッセージまたはファイルを送信
     */
    public void sendMessageOrFile(String token, Long roomId, String message, String filePath) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Chatwork token is not set.");
        }

        try {
            if (filePath != null && !filePath.isBlank()) {
                sendFile(token, roomId, message, filePath);
            } else {
                sendMessage(token, roomId, message);
            }
        } catch (RestClientResponseException e) {
            log.error("❌ Failed to send to roomId={}: HTTP {}: {}",
                    roomId, e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(e);
        } catch (Exception e) {
            log.error("❌ Failed to send to roomId={}: {}", roomId, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 💬 テキストメッセージ送信
     */
    private void sendMessage(String token, Long roomId, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-ChatWorkToken", token);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("body", message);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                CHATWORK_API_BASE + "/rooms/" + roomId + "/messages",
                entity,
                String.class
        );

        log.info("✅ メッセージ送信成功 (roomId={}): {}", roomId, response.getStatusCode());
    }

    /**
     * 📎 ファイルアップロード（Java標準APIのみ使用）
     */
    private void sendFile(String token, Long roomId, String message, String filePath) {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) {
            throw new IllegalArgumentException("ファイルが存在しないか空です: " + filePath);
        }

        String boundary = "----ChatworkBoundary" + System.currentTimeMillis();
        String urlStr = CHATWORK_API_BASE + "/rooms/" + roomId + "/files";

        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("X-ChatWorkToken", token);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream out = conn.getOutputStream();
                 PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8), true)) {

                // --- ファイル部 ---
                writer.append("--").append(boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                      .append(file.getName()).append("\"\r\n");
                writer.append("Content-Type: application/octet-stream\r\n\r\n");
                writer.flush();

                Files.copy(file.toPath(), out);
                out.flush();
                writer.append("\r\n");

                // --- メッセージ部（任意） ---
                if (message != null && !message.isBlank()) {
                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"message\"\r\n\r\n");
                    writer.append(message).append("\r\n");
                    writer.flush();
                }

                // --- 終端 ---
                writer.append("--").append(boundary).append("--").append("\r\n");
                writer.flush();
            }

            int responseCode = conn.getResponseCode();
            InputStream responseStream = (responseCode >= 200 && responseCode < 300)
                    ? conn.getInputStream() : conn.getErrorStream();
            String responseBody = new String(responseStream.readAllBytes(), StandardCharsets.UTF_8);

            if (responseCode >= 200 && responseCode < 300) {
                log.info("✅ ファイル送信成功: roomId={} status={} body={}", roomId, responseCode, responseBody);
            } else {
                log.error("❌ ファイル送信失敗: HTTP {} → {}", responseCode, responseBody);
                throw new RuntimeException("Chatwork file upload failed: " + responseBody);
            }

        } catch (Exception e) {
            log.error("❌ Failed to send to roomId={}: {}", roomId, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 🧾 ルーム名取得
     */
    public String getRoomName(Long roomId, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-ChatWorkToken", token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<ChatworkRoom> response = restTemplate.exchange(
                    CHATWORK_API_BASE + "/rooms/" + roomId,
                    HttpMethod.GET,
                    entity,
                    ChatworkRoom.class
            );

            return response.getBody() != null ? response.getBody().getName() : "Unknown Room";
        } catch (Exception e) {
            log.warn("⚠️ ルーム名取得失敗 (roomId={}): {}", roomId, e.getMessage());
            return "Unknown Room";
        }
    }

    /**
     * 🕒 複数ルームへ5秒間隔で送信（API制限対策）
     */
    public void sendWithInterval(String token, List<Long> roomIds, String message, String filePath) {
        for (int i = 0; i < roomIds.size(); i++) {
            Long roomId = roomIds.get(i);
            sendMessageOrFile(token, roomId, message, filePath);

            if (i < roomIds.size() - 1) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
