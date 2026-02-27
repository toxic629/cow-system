package com.cowhealth.observation;

import com.cowhealth.common.ApiResponse;
import com.cowhealth.observation.dto.AggPoint;
import com.cowhealth.observation.service.ObservationService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/observations")
public class ObservationQueryController {

    private final ObservationService observationService;

    public ObservationQueryController(ObservationService observationService) {
        this.observationService = observationService;
    }

    @GetMapping("/query")
    public ApiResponse<List<AggPoint>> query(@RequestParam String cowId,
                                             @RequestParam String barnId,
                                             @RequestParam String metric,
                                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime from,
                                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime to,
                                             @RequestParam(defaultValue = "minute") String granularity) {
        return ApiResponse.ok(observationService.query(cowId, barnId, metric, from, to, granularity));
    }
}
