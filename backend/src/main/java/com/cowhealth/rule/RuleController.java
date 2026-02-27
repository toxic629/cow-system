package com.cowhealth.rule;

import com.cowhealth.common.ApiResponse;
import com.cowhealth.common.PageResult;
import com.cowhealth.rule.entity.RuleConfig;
import com.cowhealth.rule.service.RuleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

    private final RuleService ruleService;

    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping
    public ApiResponse<PageResult<RuleConfig>> page(@RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(ruleService.page(page, size));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody RuleConfig rule) {
        ruleService.create(rule);
        return ApiResponse.ok();
    }

    @PutMapping
    public ApiResponse<Void> update(@Valid @RequestBody RuleConfig rule) {
        ruleService.update(rule);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        ruleService.delete(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/enable")
    public ApiResponse<Void> enable(@PathVariable Long id) {
        ruleService.setEnabled(id, true);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<Void> disable(@PathVariable Long id) {
        ruleService.setEnabled(id, false);
        return ApiResponse.ok();
    }
}
