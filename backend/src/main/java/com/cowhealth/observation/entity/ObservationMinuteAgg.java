package com.cowhealth.observation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("observation_minute_agg")
public class ObservationMinuteAgg {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String cowId;
    private String barnId;
    private String metricName;
    private LocalDateTime tsMinute;
    private Double sumValue;
    private Double minValue;
    private Double maxValue;
    private Long countValue;
    private Long badCount;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
