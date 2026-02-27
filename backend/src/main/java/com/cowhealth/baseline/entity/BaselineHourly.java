package com.cowhealth.baseline.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("baseline_hourly")
public class BaselineHourly {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String cowId;
    private String metricName;
    private Integer hourOfDay;
    private Double mean;
    private Double std;
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
