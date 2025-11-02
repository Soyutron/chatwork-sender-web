package com.example.chatwork.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
@Table(name = "group_sets")
public class GroupSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String name;

    @ElementCollection
    @CollectionTable(name = "group_set_rooms", joinColumns = @JoinColumn(name = "group_set_id"))
    @Column(name = "room_id")
    private List<Long> roomIds;
}
