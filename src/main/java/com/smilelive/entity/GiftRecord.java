package com.smilelive.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("gift_record")
@Data
public class GiftRecord {
    private Long id;
    private Long receiver;
    private Long giver;
    @TableField(exist = false)
    private String giverName;
    @TableField(value = "gift_id")
    private Long giftId;
    @TableField(exist = false)
    private String giftName;
    @TableField(exist = false)
    private Integer giftValue;
    @TableField(exist = false)
    private String giftImage;
    private Integer count;
    private LocalDateTime time;
}
