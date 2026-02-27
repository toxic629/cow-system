package com.cowhealth.observation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cowhealth.observation.dto.AggPoint;
import com.cowhealth.observation.entity.ObservationMinuteAgg;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ObservationMinuteAggMapper extends BaseMapper<ObservationMinuteAgg> {

    @Select("""
            <script>
            SELECT ts_minute AS ts,
                   CASE WHEN count_value = 0 THEN NULL ELSE sum_value / count_value END AS avgValue,
                   min_value AS minValue,
                   max_value AS maxValue,
                   count_value AS countValue,
                   bad_count AS badCount
            FROM observation_minute_agg
            WHERE cow_id = #{cowId}
              AND barn_id = #{barnId}
              AND metric_name = #{metric}
              AND ts_minute BETWEEN #{from} AND #{to}
            ORDER BY ts_minute
            </script>
            """)
    List<AggPoint> queryMinute(@Param("cowId") String cowId,
                               @Param("barnId") String barnId,
                               @Param("metric") String metric,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);

    @Select("""
            <script>
            SELECT CAST(DATE_FORMAT(ts_minute,
                CASE WHEN #{granularity}='hour' THEN '%Y-%m-%d %H:00:00' ELSE '%Y-%m-%d 00:00:00' END) AS DATETIME) AS ts,
                   CASE WHEN SUM(count_value)=0 THEN NULL ELSE SUM(sum_value)/SUM(count_value) END AS avgValue,
                   MIN(min_value) AS minValue,
                   MAX(max_value) AS maxValue,
                   SUM(count_value) AS countValue,
                   SUM(bad_count) AS badCount
            FROM observation_minute_agg
            WHERE cow_id = #{cowId}
              AND barn_id = #{barnId}
              AND metric_name = #{metric}
              AND ts_minute BETWEEN #{from} AND #{to}
            GROUP BY DATE_FORMAT(ts_minute,
                CASE WHEN #{granularity}='hour' THEN '%Y-%m-%d %H:00:00' ELSE '%Y-%m-%d 00:00:00' END)
            ORDER BY ts
            </script>
            """)
    List<AggPoint> queryRollup(@Param("cowId") String cowId,
                               @Param("barnId") String barnId,
                               @Param("metric") String metric,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to,
                               @Param("granularity") String granularity);

    @Select("""
            SELECT * FROM observation_minute_agg
            WHERE cow_id = #{cowId}
              AND metric_name = #{metric}
            ORDER BY ts_minute DESC
            LIMIT #{n}
            """)
    List<ObservationMinuteAgg> latestN(@Param("cowId") String cowId, @Param("metric") String metric, @Param("n") int n);

    @Select("""
            SELECT * FROM observation_minute_agg
            WHERE cow_id = #{cowId}
              AND metric_name = #{metric}
            ORDER BY ts_minute DESC
            LIMIT 1
            """)
    ObservationMinuteAgg latestOne(@Param("cowId") String cowId, @Param("metric") String metric);

    @Select("""
            SELECT * FROM observation_minute_agg
            WHERE barn_id = #{barnId}
              AND cow_id = 'ENV'
              AND metric_name = #{metric}
            ORDER BY ts_minute DESC
            LIMIT 1
            """)
    ObservationMinuteAgg latestBarnEnv(@Param("barnId") String barnId, @Param("metric") String metric);
}
