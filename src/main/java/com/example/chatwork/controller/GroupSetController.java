package com.example.chatwork.controller;

import com.example.chatwork.entity.GroupSet;
import com.example.chatwork.model.GroupSetRequest;
import com.example.chatwork.model.RenameRequest;
import com.example.chatwork.service.GroupSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groupset")
public class GroupSetController {

    private final GroupSetService groupSetService;

    /** ユーザーのグループセット一覧取得 */
    @GetMapping
    public ResponseEntity<List<GroupSet>> getGroupSets(Authentication auth) {
        List<GroupSet> list = groupSetService.findByUser(auth.getName());
        return ResponseEntity.ok(list);
    }

    /** 新規 or 上書き保存 */
    @PostMapping
    public ResponseEntity<Map<String, String>> saveOrUpdateGroupSet(
            @RequestBody GroupSetRequest req, Authentication auth) {

        boolean updated = groupSetService.saveOrUpdateGroupSet(auth.getName(), req);
        String msg = updated ? "上書き保存しました" : "新規セットを作成しました";
        return ResponseEntity.ok(Map.of("message", msg));
    }

    /** 削除 */
    @DeleteMapping("/{name}")
    public ResponseEntity<Map<String, String>> deleteGroupSet(
            @PathVariable String name, Authentication auth) {
        groupSetService.deleteGroupSet(auth.getName(), name);
        return ResponseEntity.ok(Map.of("message", "削除しました"));
    }

    /** 名前変更 */
    @PostMapping("/rename")
    public ResponseEntity<Map<String, String>> renameGroupSet(
            @RequestBody RenameRequest req, Authentication auth) {
        groupSetService.renameGroupSet(auth.getName(), req.getOldName(), req.getNewName());
        return ResponseEntity.ok(Map.of("message", "名前を変更しました"));
    }
}
