package com.cowhealth.alarm.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cowhealth.alarm.entity.Alarm;
import com.cowhealth.alarm.entity.AlarmActionLog;
import com.cowhealth.alarm.mapper.AlarmActionLogMapper;
import com.cowhealth.alarm.mapper.AlarmMapper;
import com.cowhealth.common.BusinessException;
import com.cowhealth.common.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class AlarmService {

    private final AlarmMapper alarmMapper;
    private final AlarmActionLogMapper actionLogMapper;

    public AlarmService(AlarmMapper alarmMapper, AlarmActionLogMapper actionLogMapper) {
        this.alarmMapper = alarmMapper;
        this.actionLogMapper = actionLogMapper;
    }

    public PageResult<Alarm> page(long page, long size, String severity, String status, String cowId, String barnId,
                                  String metric, LocalDateTime from, LocalDateTime to) {
        LambdaQueryWrapper<Alarm> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(severity), Alarm::getSeverity, severity)
                .eq(StringUtils.hasText(status), Alarm::getStatus, status)
                .eq(StringUtils.hasText(cowId), Alarm::getCowId, cowId)
                .eq(StringUtils.hasText(barnId), Alarm::getBarnId, barnId)
                .eq(StringUtils.hasText(metric), Alarm::getMetricName, metric)
                .ge(from != null, Alarm::getStartTime, from)
                .le(to != null, Alarm::getStartTime, to)
                .orderByDesc(Alarm::getStartTime);
        Page<Alarm> p = alarmMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(page, size, p.getTotal(), p.getRecords());
    }

    public Alarm getById(Long id) {
        Alarm alarm = alarmMapper.selectById(id);
        if (alarm == null) {
            throw new BusinessException(404, "Alarm not found");
        }
        return alarm;
    }

    @Transactional
    public Alarm createAlarm(Alarm alarm) {
        LocalDateTime now = LocalDateTime.now();
        alarm.setStatus("OPEN");
        alarm.setCreatedAt(now);
        alarm.setUpdatedAt(now);
        if (alarm.getStartTime() == null) {
            alarm.setStartTime(now);
        }
        alarmMapper.insert(alarm);
        return alarm;
    }

    public boolean existsOpen(Long ruleId, String cowId) {
        return alarmMapper.selectCount(new LambdaQueryWrapper<Alarm>()
                .eq(Alarm::getRuleId, ruleId)
                .eq(Alarm::getCowId, cowId)
                .in(Alarm::getStatus, "OPEN", "ACKED")) > 0;
    }

    @Transactional
    public void ack(Long id, String note, String operator) {
        updateStatus(id, "ACKED", "ACK", note, operator, false);
    }

    @Transactional
    public void resolve(Long id, String note, String operator) {
        updateStatus(id, "RESOLVED", "RESOLVE", note, operator, true);
    }

    @Transactional
    public void falsePositive(Long id, String note, String operator) {
        updateStatus(id, "FALSE_POSITIVE", "FALSE_POSITIVE", note, operator, true);
    }

    private void updateStatus(Long id, String targetStatus, String action, String note, String operator, boolean setEndTime) {
        Alarm alarm = getById(id);
        alarm.setStatus(targetStatus);
        if (setEndTime) {
            alarm.setEndTime(LocalDateTime.now());
        }
        alarm.setUpdatedAt(LocalDateTime.now());
        alarmMapper.updateById(alarm);

        AlarmActionLog log = new AlarmActionLog();
        log.setAlarmId(id);
        log.setAction(action);
        log.setNote(note);
        log.setOperator(StringUtils.hasText(operator) ? operator : "system");
        log.setCreatedAt(LocalDateTime.now());
        actionLogMapper.insert(log);
    }
}
