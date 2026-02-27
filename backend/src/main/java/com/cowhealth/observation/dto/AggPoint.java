package com.cowhealth.observation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AggPoint {
    private LocalDateTime ts;
    private Double avgValue;
    private Double minValue;
    private Double maxValue;
    private Long countValue;
    private Long badCount;
}
