package com.cowhealth.observation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("observation_raw")
public class ObservationRaw {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String cowId;
    private String barnId;
    private String metricName;
    private LocalDateTime ts;
    private Double value;
    private String qualityFlag;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
