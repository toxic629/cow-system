package com.cowhealth.dashboard.service;

import com.cowhealth.dashboard.dto.DashboardSummary;
import com.cowhealth.dashboard.mapper.DashboardMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final DashboardMapper dashboardMapper;

    public DashboardService(DashboardMapper dashboardMapper) {
        this.dashboardMapper = dashboardMapper;
    }

    public DashboardSummary summary() {
        Map<String, Long> severity = new HashMap<>();
        for (Map<String, Object> row : dashboardMapper.todayAlarmBySeverity()) {
            severity.put(String.valueOf(row.get("k")), ((Number) row.get("v")).longValue());
        }
        long observe = 0;
        long treatment = 0;
        for (Map<String, Object> row : dashboardMapper.cowStatusCounts()) {
            String status = String.valueOf(row.get("k"));
            long cnt = ((Number) row.get("v")).longValue();
            if ("OBSERVE".equals(status)) {
                observe = cnt;
            } else if ("TREATMENT".equals(status)) {
                treatment = cnt;
            }
        }
        List<Map<String, Object>> trend = dashboardMapper.trend24h();
        List<Map<String, Object>> top = dashboardMapper.topMetrics();
        return new DashboardSummary(severity, observe, treatment, trend, top);
    }
}
