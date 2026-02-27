package com.cowhealth.schedule;

import com.cowhealth.baseline.service.BaselineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BaselineScheduler {

    private final BaselineService baselineService;

    public BaselineScheduler(BaselineService baselineService) {
        this.baselineService = baselineService;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void rebuildBaselineDaily() {
        baselineService.rebuild();
        log.info("baseline rebuild done");
    }
}
