package com.cowhealth.alarm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alarm_action_log")
public class AlarmActionLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long alarmId;
    private String action;
    private String note;
    private String operator;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
