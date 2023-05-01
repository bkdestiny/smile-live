package com.smilelive.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupMsg {
    private Long roomId;
    private Long sender;
    private String nickname;
    private String content;
    /*1.SYSTEM 2.USER*/
    private String type;
    private LocalDateTime created;
}
