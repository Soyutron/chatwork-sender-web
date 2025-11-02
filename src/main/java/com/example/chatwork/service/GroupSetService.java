package com.example.chatwork.service;

import com.example.chatwork.entity.GroupSet;
import com.example.chatwork.entity.User;
import com.example.chatwork.model.GroupSetRequest;
import com.example.chatwork.repository.GroupSetRepository;
import com.example.chatwork.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupSetService {

    private final GroupSetRepository groupSetRepo;
    private final UserRepository userRepo;

    /** ユーザー別セット一覧取得 */
    @Transactional(readOnly = true)
    public List<GroupSet> findByUser(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        return groupSetRepo.findByUser(user);
    }

    /** 上書き or 新規登録 */
    @Transactional
    public boolean saveOrUpdateGroupSet(String username, GroupSetRequest req) {
        User user = userRepo.findByUsername(username).orElseThrow();

        Optional<GroupSet> existingOpt = groupSetRepo.findByUserAndName(user, req.getName());
        if (existingOpt.isPresent()) {
            GroupSet existing = existingOpt.get();
            existing.setRoomIds(req.getRoomIds());
            groupSetRepo.save(existing);
            return true; // 上書き
        }

        GroupSet gs = new GroupSet();
        gs.setUser(user);
        gs.setName(req.getName());
        gs.setRoomIds(req.getRoomIds());
        groupSetRepo.save(gs);
        return false; // 新規
    }

    /** 削除 */
    @Transactional
    public void deleteGroupSet(String username, String name) {
        User user = userRepo.findByUsername(username).orElseThrow();
        groupSetRepo.deleteByUserAndName(user, name);
    }

    /** 名前変更 */
    @Transactional
    public void renameGroupSet(String username, String oldName, String newName) {
        User user = userRepo.findByUsername(username).orElseThrow();

        GroupSet gs = groupSetRepo.findByUserAndName(user, oldName)
                .orElseThrow(() -> new IllegalArgumentException("グループセットが存在しません"));

        // 名前重複チェック
        groupSetRepo.findByUserAndName(user, newName).ifPresent(x -> {
            throw new IllegalArgumentException("同じ名前のセットが既に存在します");
        });

        gs.setName(newName);
        groupSetRepo.save(gs);
    }
}
