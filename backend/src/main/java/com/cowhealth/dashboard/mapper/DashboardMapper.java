package com.cowhealth.dashboard.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {

    @Select("""
            SELECT severity AS k, COUNT(*) AS v
            FROM alarm
            WHERE DATE(start_time)=CURDATE()
            GROUP BY severity
            """)
    List<Map<String, Object>> todayAlarmBySeverity();

    @Select("""
            SELECT status AS k, COUNT(*) AS v
            FROM cow
            WHERE status IN ('OBSERVE','TREATMENT')
            GROUP BY status
            """)
    List<Map<String, Object>> cowStatusCounts();

    @Select("""
            SELECT DATE_FORMAT(start_time, '%Y-%m-%d %H:00:00') AS hourBucket, COUNT(*) AS cnt
            FROM alarm
            WHERE start_time >= DATE_SUB(NOW(), INTERVAL 24 HOUR)
            GROUP BY DATE_FORMAT(start_time, '%Y-%m-%d %H:00:00')
            ORDER BY hourBucket
            """)
    List<Map<String, Object>> trend24h();

    @Select("""
            SELECT metric_name AS metric, COUNT(*) AS cnt
            FROM alarm
            WHERE DATE(start_time)=CURDATE()
            GROUP BY metric_name
            ORDER BY cnt DESC
            LIMIT 5
            """)
    List<Map<String, Object>> topMetrics();
}
