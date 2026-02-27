package com.cowhealth.observation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ObservationItem {
    @NotBlank
    private String cowId;
    @NotBlank
    private String barnId;
    @NotBlank
    private String metricName;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    @NotNull
    private Double value;
}
