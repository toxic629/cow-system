package com.cowhealth.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DashboardSummary {
    private Map<String, Long> todayAlarmBySeverity;
    private long observingCows;
    private long treatmentCows;
    private List<Map<String, Object>> trend24h;
    private List<Map<String, Object>> topMetrics;
}
