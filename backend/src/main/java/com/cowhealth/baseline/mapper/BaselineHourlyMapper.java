package com.cowhealth.baseline.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cowhealth.baseline.entity.BaselineHourly;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BaselineHourlyMapper extends BaseMapper<BaselineHourly> {

    @Delete("DELETE FROM baseline_hourly")
    void truncateAll();

    @Insert("""
            INSERT INTO baseline_hourly(cow_id, metric_name, hour_of_day, mean, std, updated_at)
            SELECT cow_id,
                   metric_name,
                   HOUR(ts_minute),
                   AVG(CASE WHEN count_value=0 THEN NULL ELSE sum_value/count_value END) AS mean_val,
                   STDDEV_SAMP(CASE WHEN count_value=0 THEN NULL ELSE sum_value/count_value END) AS std_val,
                   NOW()
            FROM observation_minute_agg
            WHERE ts_minute >= DATE_SUB(NOW(), INTERVAL 7 DAY)
            GROUP BY cow_id, metric_name, HOUR(ts_minute)
            """)
    void rebuildFromPast7Days();

    @Select("SELECT * FROM baseline_hourly WHERE cow_id=#{cowId} AND metric_name=#{metricName} AND hour_of_day=#{hourOfDay} LIMIT 1")
    BaselineHourly findOne(@Param("cowId") String cowId, @Param("metricName") String metricName, @Param("hourOfDay") Integer hourOfDay);

    @Select("SELECT DISTINCT cow_id FROM baseline_hourly WHERE cow_id <> 'ENV'")
    List<String> allCowIds();
}
