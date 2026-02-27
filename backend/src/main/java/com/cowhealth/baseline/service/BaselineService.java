package com.cowhealth.baseline.service;

import com.cowhealth.baseline.entity.BaselineHourly;
import com.cowhealth.baseline.mapper.BaselineHourlyMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BaselineService {

    private final BaselineHourlyMapper baselineHourlyMapper;

    public BaselineService(BaselineHourlyMapper baselineHourlyMapper) {
        this.baselineHourlyMapper = baselineHourlyMapper;
    }

    @Transactional
    public void rebuild() {
        baselineHourlyMapper.truncateAll();
        baselineHourlyMapper.rebuildFromPast7Days();
    }

    public double calcDeviation(String cowId, String metricName, Double currentValue, LocalDateTime ts) {
        if (currentValue == null || ts == null) {
            return 0.0;
        }
        BaselineHourly baseline = baselineHourlyMapper.findOne(cowId, metricName, ts.getHour());
        if (baseline == null || baseline.getMean() == null || baseline.getStd() == null) {
            return 0.0;
        }
        double safeStd = Math.max(Math.abs(baseline.getStd()), 0.3);
        return (currentValue - baseline.getMean()) / safeStd;
    }
}
