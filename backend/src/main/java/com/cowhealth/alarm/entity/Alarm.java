package com.cowhealth.alarm.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("alarm")
public class Alarm {
    @TableId(value = "alarm_id", type = IdType.AUTO)
    private Long alarmId;
    private String cowId;
    private String barnId;
    private Long ruleId;
    private String severity;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String metricName;
    private Double triggerValue;
    private String thresholdJson;
    private Double deviationScore;
    private String evidenceJson;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
