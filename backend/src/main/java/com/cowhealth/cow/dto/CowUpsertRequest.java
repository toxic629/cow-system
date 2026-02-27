package com.cowhealth.cow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CowUpsertRequest {

    @NotBlank
    private String cowId;
    @NotBlank
    private String name;
    @NotBlank
    private String earTag;
    @NotBlank
    private String barnId;
    private String pen;
    @NotNull
    private Integer ageDays;
    private Integer parity;
    private Integer lactationDay;
    @NotBlank
    private String status;
}
