package com.cowhealth.observation.service;

import com.cowhealth.observation.enums.QualityFlag;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class QualityService {

    private final StringRedisTemplate redisTemplate;
    private final Map<String, double[]> ranges = new HashMap<>();
    private final Map<String, Double> jumpThreshold = new HashMap<>();

    public QualityService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        ranges.put("resp_rate", new double[]{5, 120});
        ranges.put("temp_c", new double[]{30, 45});
        ranges.put("humidity", new double[]{0, 100});
        ranges.put("activity", new double[]{0, 1000});

        jumpThreshold.put("resp_rate", 25.0);
        jumpThreshold.put("temp_c", 1.2);
        jumpThreshold.put("humidity", 20.0);
        jumpThreshold.put("activity", 200.0);
    }

    public QualityFlag evaluate(String cowId, String metric, Double value) {
        if (value == null || value.isNaN() || value.isInfinite()) {
            return QualityFlag.BAD;
        }
        double[] range = ranges.getOrDefault(metric, new double[]{-1e6, 1e6});
        if (value < range[0] || value > range[1]) {
            return QualityFlag.BAD;
        }
        String key = "quality:last:" + cowId + ":" + metric;
        String oldVal = redisTemplate.opsForValue().get(key);
        redisTemplate.opsForValue().set(key, String.valueOf(value), Duration.ofHours(12));
        if (oldVal != null) {
            double prev = Double.parseDouble(oldVal);
            double threshold = jumpThreshold.getOrDefault(metric, 1000.0);
            if (Math.abs(value - prev) > threshold) {
                return QualityFlag.SUSPECT;
            }
        }
        return QualityFlag.GOOD;
    }
}
