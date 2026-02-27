package com.cowhealth.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cowhealth.alarm.entity.Alarm;
import com.cowhealth.alarm.service.AlarmService;
import com.cowhealth.baseline.service.BaselineService;
import com.cowhealth.cow.entity.Cow;
import com.cowhealth.cow.mapper.CowMapper;
import com.cowhealth.observation.entity.ObservationMinuteAgg;
import com.cowhealth.observation.mapper.ObservationMinuteAggMapper;
import com.cowhealth.rule.entity.RuleConfig;
import com.cowhealth.rule.service.RuleService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class RuleEvaluationScheduler {

    private final RuleService ruleService;
    private final CowMapper cowMapper;
    private final ObservationMinuteAggMapper aggMapper;
    private final AlarmService alarmService;
    private final BaselineService baselineService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RuleEvaluationScheduler(RuleService ruleService, CowMapper cowMapper, ObservationMinuteAggMapper aggMapper,
                                   AlarmService alarmService, BaselineService baselineService,
                                   StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.ruleService = ruleService;
        this.cowMapper = cowMapper;
        this.aggMapper = aggMapper;
        this.alarmService = alarmService;
        this.baselineService = baselineService;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(cron = "0 * * * * *")
    public void evaluateRulesEveryMinute() {
        List<RuleConfig> rules = ruleService.listEnabled();
        if (rules.isEmpty()) {
            return;
        }
        List<Cow> cows = cowMapper.selectList(new LambdaQueryWrapper<Cow>().ne(Cow::getCowId, "ENV"));
        for (RuleConfig rule : rules) {
            for (Cow cow : cows) {
                if (StringUtils.hasText(rule.getBarnScope()) && !Objects.equals(rule.getBarnScope(), cow.getBarnId())) {
                    continue;
                }
                try {
                    switch (rule.getType()) {
                        case "THRESHOLD" -> evalThreshold(rule, cow);
                        case "DURATION" -> evalDuration(rule, cow);
                        case "COMPOSITE" -> evalComposite(rule, cow);
                        case "OFFLINE" -> evalOffline(rule, cow);
                        default -> log.warn("unsupported rule type: {}", rule.getType());
                    }
                } catch (Exception ex) {
                    log.error("rule eval failed rule={} cow={}", rule.getRuleId(), cow.getCowId(), ex);
                }
            }
        }
    }

    private void evalThreshold(RuleConfig rule, Cow cow) {
        ObservationMinuteAgg latest = aggMapper.latestOne(cow.getCowId(), rule.getMetricName());
        if (latest == null || latest.getCountValue() == 0) {
            return;
        }
        double current = latest.getSumValue() / latest.getCountValue();
        if (violates(current, rule.getUpper(), rule.getLower())) {
            triggerAlarm(rule, cow, latest.getTsMinute(), current, "threshold");
        }
    }

    private void evalDuration(RuleConfig rule, Cow cow) {
        int n = Math.max(rule.getDurationMinutes() == null ? 1 : rule.getDurationMinutes(), 1);
        List<ObservationMinuteAgg> lastN = aggMapper.latestN(cow.getCowId(), rule.getMetricName(), n);
        if (lastN.size() < n) {
            return;
        }
        for (ObservationMinuteAgg agg : lastN) {
            if (agg.getCountValue() == 0) {
                return;
            }
            double avg = agg.getSumValue() / agg.getCountValue();
            if (!violates(avg, rule.getUpper(), rule.getLower())) {
                return;
            }
        }
        ObservationMinuteAgg latest = lastN.get(0);
        double latestAvg = latest.getSumValue() / latest.getCountValue();
        triggerAlarm(rule, cow, latest.getTsMinute(), latestAvg, "duration");
    }

    private void evalComposite(RuleConfig rule, Cow cow) throws Exception {
        if (!StringUtils.hasText(rule.getCompositeJson())) {
            return;
        }
        Map<String, Object> c = objectMapper.readValue(rule.getCompositeJson(), new TypeReference<>() {});
        String metric = String.valueOf(c.getOrDefault("metric", rule.getMetricName()));
        double metricUpper = ((Number) c.getOrDefault("upper", rule.getUpper() == null ? 0d : rule.getUpper())).doubleValue();
        String tempMetric = String.valueOf(c.getOrDefault("tempMetric", "temp_c"));
        String humidityMetric = String.valueOf(c.getOrDefault("humidityMetric", "humidity"));
        double tempUpper = ((Number) c.getOrDefault("tempUpper", 39.0)).doubleValue();
        double humidityUpper = ((Number) c.getOrDefault("humidityUpper", 80.0)).doubleValue();

        ObservationMinuteAgg cowMetric = aggMapper.latestOne(cow.getCowId(), metric);
        ObservationMinuteAgg temp = aggMapper.latestBarnEnv(cow.getBarnId(), tempMetric);
        ObservationMinuteAgg humidity = aggMapper.latestBarnEnv(cow.getBarnId(), humidityMetric);
        if (cowMetric == null || temp == null || humidity == null ||
                cowMetric.getCountValue() == 0 || temp.getCountValue() == 0 || humidity.getCountValue() == 0) {
            return;
        }

        double cowValue = cowMetric.getSumValue() / cowMetric.getCountValue();
        double t = temp.getSumValue() / temp.getCountValue();
        double h = humidity.getSumValue() / humidity.getCountValue();
        boolean hit = cowValue > metricUpper && (t > tempUpper && h > humidityUpper);
        if (hit) {
            triggerAlarm(rule, cow, cowMetric.getTsMinute(), cowValue,
                    "composite temp=" + t + ",humidity=" + h);
        }
    }

    private void evalOffline(RuleConfig rule, Cow cow) {
        ObservationMinuteAgg latest = aggMapper.latestOne(cow.getCowId(), rule.getMetricName());
        int missMin = Math.max(rule.getDurationMinutes() == null ? 30 : rule.getDurationMinutes(), 1);
        boolean offline = latest == null || Duration.between(latest.getTsMinute(), LocalDateTime.now()).toMinutes() >= missMin;
        if (offline) {
            triggerAlarm(rule, cow, LocalDateTime.now(), null, "offline");
        }
    }

    private void triggerAlarm(RuleConfig rule, Cow cow, LocalDateTime ts, Double triggerValue, String evidence) {
        String silenceKey = "silence:" + cow.getCowId() + ":" + rule.getRuleId();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(silenceKey))) {
            return;
        }
        if (alarmService.existsOpen(rule.getRuleId(), cow.getCowId())) {
            return;
        }
        Alarm alarm = new Alarm();
        alarm.setCowId(cow.getCowId());
        alarm.setBarnId(cow.getBarnId());
        alarm.setRuleId(rule.getRuleId());
        alarm.setSeverity(rule.getSeverity());
        alarm.setMetricName(rule.getMetricName());
        alarm.setStartTime(ts);
        alarm.setTriggerValue(triggerValue);
        alarm.setThresholdJson("{\"upper\":" + rule.getUpper() + ",\"lower\":" + rule.getLower() + "}");
        double deviation = baselineService.calcDeviation(cow.getCowId(), rule.getMetricName(), triggerValue, ts);
        alarm.setDeviationScore(deviation);
        alarm.setEvidenceJson("{\"reason\":\"" + evidence + "\",\"deviationScore\":" + deviation + "}");
        alarmService.createAlarm(alarm);
        int silenceMinutes = Math.max(rule.getSilenceMinutes() == null ? 30 : rule.getSilenceMinutes(), 1);
        redisTemplate.opsForValue().set(silenceKey, "1", Duration.ofMinutes(silenceMinutes));
    }

    private boolean violates(double value, Double upper, Double lower) {
        if (upper != null && value > upper) {
            return true;
        }
        return lower != null && value < lower;
    }
}
