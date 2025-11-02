package com.example.chatwork.repository;

import com.example.chatwork.entity.Setting;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ============================================================
 * SettingRepository
 * 機能: Settingエンティティの永続化操作
 * ============================================================
 */
public interface SettingRepository extends JpaRepository<Setting, Long> {
}
