package com.cowhealth.cow;

import com.cowhealth.common.ApiResponse;
import com.cowhealth.common.PageResult;
import com.cowhealth.cow.dto.CowUpsertRequest;
import com.cowhealth.cow.entity.Cow;
import com.cowhealth.cow.service.CowService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cows")
public class CowController {

    private final CowService cowService;

    public CowController(CowService cowService) {
        this.cowService = cowService;
    }

    @GetMapping
    public ApiResponse<PageResult<Cow>> page(@RequestParam(defaultValue = "1") long page,
                                             @RequestParam(defaultValue = "10") long size,
                                             @RequestParam(required = false) String barnId,
                                             @RequestParam(required = false) String status,
                                             @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(cowService.page(page, size, barnId, status, keyword));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CowUpsertRequest request) {
        cowService.create(request);
        return ApiResponse.ok();
    }

    @PutMapping
    public ApiResponse<Void> update(@Valid @RequestBody CowUpsertRequest request) {
        cowService.update(request);
        return ApiResponse.ok();
    }

    @DeleteMapping
    public ApiResponse<Void> delete(@RequestParam String cowId) {
        cowService.delete(cowId);
        return ApiResponse.ok();
    }
}
