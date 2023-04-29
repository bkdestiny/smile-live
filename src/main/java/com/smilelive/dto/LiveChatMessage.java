package com.smilelive.dto;

import lombok.Data;

@Data
public class LiveChatMessage {
    private long userId;
    private long roomId;
    private String username;
    private String content;
    private String type;
}
