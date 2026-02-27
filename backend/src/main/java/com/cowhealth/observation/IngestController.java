package com.cowhealth.observation;

import com.cowhealth.common.ApiResponse;
import com.cowhealth.observation.dto.CsvImportResult;
import com.cowhealth.observation.dto.IngestResult;
import com.cowhealth.observation.dto.ObservationItem;
import com.cowhealth.observation.service.ObservationService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/ingest")
public class IngestController {

    private final ObservationService observationService;

    public IngestController(ObservationService observationService) {
        this.observationService = observationService;
    }

    @PostMapping("/observations")
    public ApiResponse<IngestResult> ingest(@Valid @RequestBody List<ObservationItem> items) {
        return ApiResponse.ok(observationService.ingestBatch(items));
    }

    @PostMapping("/csv")
    public ApiResponse<CsvImportResult> importCsv(@RequestParam("file") MultipartFile file) {
        return ApiResponse.ok(observationService.importCsv(file));
    }
}
