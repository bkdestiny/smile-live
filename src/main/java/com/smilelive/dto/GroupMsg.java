package com.smilelive.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMsg {
    private Long roomId;
    private Long sender;
    private String senderName;
    private String content;
    /*1.SYSTEM 2.USER 3.GIFT*/
    private String type;
    private Object data;
    /*用户聊天消息*/
    public static GroupMsg createUserMsg(Long roomId,Long sender,String senderName,String content){
        return new GroupMsg (roomId,sender,senderName,content,"USER",null);
    }
    /*系统聊天消息*/
    public static GroupMsg createSystemMsg(Long roomId,String content){
        return new GroupMsg (roomId,null,"系统消息",content,"SYSTEM",null);
    }
    /*发送礼物消息*/
    public static GroupMsg createGiftMsg(Long roomId,Long sender,String senderName,String content,Object data){
        return new GroupMsg (roomId,sender,senderName,content,"GIFT",data);
    }
}
