package com.example.chatwork.model;

import lombok.Data;

@Data
public class RenameRequest {
    private String oldName;
    private String newName;
}
