package com.cowhealth.observation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IngestResult {
    private int successCount;
    private int duplicateCount;
    private int badCount;
}
