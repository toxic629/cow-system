package com.cowhealth.observation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CsvImportResult {
    private int importedCount;
    private List<String> errorRows;
}
