package com.cowhealth.alarm.dto;

import lombok.Data;

@Data
public class AlarmActionRequest {
    private String note;
    private String operator;
}
