package com.example.chatwork.repository;

import com.example.chatwork.entity.GroupSet;
import com.example.chatwork.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupSetRepository extends JpaRepository<GroupSet, Long> {

    List<GroupSet> findByUser(User user);

    Optional<GroupSet> findByUserAndName(User user, String name);

    @Modifying
    @Query("DELETE FROM GroupSet g WHERE g.user = :user AND g.name = :name")
    void deleteByUserAndName(@Param("user") User user, @Param("name") String name);
}
