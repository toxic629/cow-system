package com.cowhealth.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DataSeeder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        Long cnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM observation_minute_agg", Long.class);
        if (cnt != null && cnt > 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime start = now.minusDays(7);
        Random r = new Random(42);
        List<Object[]> batch = new ArrayList<>();

        for (int i = 1; i <= 30; i++) {
            String cowId = String.format("COW%03d", i);
            String barnId = i <= 15 ? "B1" : "B2";
            LocalDateTime t = start;
            while (!t.isAfter(now)) {
                if (!(i == 30 && t.isAfter(now.minusMinutes(180)))) {
                    double resp = 40 + r.nextGaussian() * 4;
                    if (i == 1 && t.isAfter(now.minusMinutes(30))) {
                        resp = 85 + r.nextGaussian() * 2;
                    }
                    batch.add(row(cowId, barnId, "resp_rate", t, resp, 1L, 0L));
                }
                double act = 250 + r.nextGaussian() * 40;
                batch.add(row(cowId, barnId, "activity", t, Math.max(act, 0), 1L, 0L));
                t = t.plusMinutes(10);
            }
        }

        for (String barnId : List.of("B1", "B2")) {
            LocalDateTime t = start;
            while (!t.isAfter(now)) {
                double temp = 36.8 + r.nextGaussian() * 0.3;
                double hum = 65 + r.nextGaussian() * 6;
                if ("B1".equals(barnId) && t.isAfter(now.minusHours(3))) {
                    temp = 39.6 + r.nextGaussian() * 0.2;
                    hum = 85 + r.nextGaussian() * 3;
                }
                batch.add(row("ENV", barnId, "temp_c", t, temp, 1L, 0L));
                batch.add(row("ENV", barnId, "humidity", t, hum, 1L, 0L));
                t = t.plusMinutes(10);
            }
        }

        jdbcTemplate.batchUpdate("""
                INSERT INTO observation_minute_agg
                (cow_id,barn_id,metric_name,ts_minute,sum_value,min_value,max_value,count_value,bad_count,updated_at)
                VALUES (?,?,?,?,?,?,?,?,?,NOW())
                ON DUPLICATE KEY UPDATE
                sum_value=VALUES(sum_value),
                min_value=VALUES(min_value),
                max_value=VALUES(max_value),
                count_value=VALUES(count_value),
                bad_count=VALUES(bad_count),
                updated_at=NOW()
                """, batch);
    }

    private Object[] row(String cowId, String barnId, String metric, LocalDateTime ts, double value, Long cnt, Long bad) {
        return new Object[]{
                cowId, barnId, metric, Timestamp.valueOf(ts),
                value, value, value, cnt, bad
        };
    }
}
