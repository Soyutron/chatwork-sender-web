package com.example.chatwork.model;

import lombok.Data;
import java.util.List;

@Data
public class GroupSetRequest {
    private String name;
    private List<Long> roomIds;
}
