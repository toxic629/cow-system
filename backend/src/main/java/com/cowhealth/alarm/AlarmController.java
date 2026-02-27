package com.cowhealth.alarm;

import com.cowhealth.alarm.dto.AlarmActionRequest;
import com.cowhealth.alarm.entity.Alarm;
import com.cowhealth.alarm.service.AlarmService;
import com.cowhealth.common.ApiResponse;
import com.cowhealth.common.PageResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/alarms")
public class AlarmController {

    private final AlarmService alarmService;

    public AlarmController(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @GetMapping
    public ApiResponse<PageResult<Alarm>> page(@RequestParam(defaultValue = "1") long page,
                                               @RequestParam(defaultValue = "10") long size,
                                               @RequestParam(required = false) String severity,
                                               @RequestParam(required = false) String status,
                                               @RequestParam(required = false) String cowId,
                                               @RequestParam(required = false) String barnId,
                                               @RequestParam(required = false) String metric,
                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime from,
                                               @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime to) {
        return ApiResponse.ok(alarmService.page(page, size, severity, status, cowId, barnId, metric, from, to));
    }

    @GetMapping("/{id}")
    public ApiResponse<Alarm> detail(@PathVariable Long id) {
        return ApiResponse.ok(alarmService.getById(id));
    }

    @PostMapping("/{id}/ack")
    public ApiResponse<Void> ack(@PathVariable Long id, @RequestBody(required = false) AlarmActionRequest req) {
        alarmService.ack(id, req == null ? null : req.getNote(), req == null ? null : req.getOperator());
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/resolve")
    public ApiResponse<Void> resolve(@PathVariable Long id, @RequestBody(required = false) AlarmActionRequest req) {
        alarmService.resolve(id, req == null ? null : req.getNote(), req == null ? null : req.getOperator());
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/false_positive")
    public ApiResponse<Void> falsePositive(@PathVariable Long id, @RequestBody(required = false) AlarmActionRequest req) {
        alarmService.falsePositive(id, req == null ? null : req.getNote(), req == null ? null : req.getOperator());
        return ApiResponse.ok();
    }
}
