package com.cowhealth.cow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cow")
public class Cow {

    @TableId(value = "cow_id", type = IdType.INPUT)
    private String cowId;
    private String name;
    private String earTag;
    private String barnId;
    private String pen;
    private Integer ageDays;
    private Integer parity;
    private Integer lactationDay;
    private String status;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
