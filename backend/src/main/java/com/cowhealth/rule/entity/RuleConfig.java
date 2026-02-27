package com.cowhealth.rule.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("rule_config")
public class RuleConfig {
    @TableId(value = "rule_id", type = IdType.AUTO)
    private Long ruleId;
    private String name;
    private Boolean enabled;
    private String severity;
    private String type;
    private String metricName;
    private Double upper;
    private Double lower;
    private Integer durationMinutes;
    private Integer silenceMinutes;
    private String compositeJson;
    private String barnScope;
    @TableField("created_at")
    private LocalDateTime createdAt;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
