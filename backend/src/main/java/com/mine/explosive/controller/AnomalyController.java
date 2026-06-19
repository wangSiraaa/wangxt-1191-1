package com.mine.explosive.controller;

import com.mine.explosive.dto.AnomalyRequest;
import com.mine.explosive.dto.ApiResponse;
import com.mine.explosive.entity.AnomalyRecord;
import com.mine.explosive.entity.User;
import com.mine.explosive.service.AnomalyService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anomalies")

@CrossOrigin(origins = "*")
public class AnomalyController {

    private final AnomalyService anomalyService;

    @Autowired
    public AnomalyController(AnomalyService anomalyService) {
        this.anomalyService = anomalyService;
    }

    @PostMapping
    public ApiResponse<AnomalyRecord> createAnomaly(
            @Valid @RequestBody AnomalyRequest request,
            Authentication authentication) {
        User reporter = (User) authentication.getPrincipal();
        return ApiResponse.success(anomalyService.createAnomaly(request, reporter));
    }

    @GetMapping("/{id}")
    public ApiResponse<AnomalyRecord> getAnomaly(@PathVariable Long id) {
        return ApiResponse.success(anomalyService.getAnomaly(id));
    }

    @GetMapping
    public ApiResponse<List<AnomalyRecord>> getAnomalies(
            @RequestParam(required = false) Long shiftId,
            @RequestParam(required = false, defaultValue = "false") boolean unresolvedOnly) {
        List<AnomalyRecord> records;
        if (shiftId != null && unresolvedOnly) {
            records = anomalyService.getUnresolvedAnomaliesByShift(shiftId);
        } else if (shiftId != null) {
            records = anomalyService.getAnomaliesByShift(shiftId);
        } else if (unresolvedOnly) {
            records = anomalyService.getUnresolvedAnomalies();
        } else {
            records = anomalyService.getAllAnomalies();
        }
        return ApiResponse.success(records);
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('SAFETY_OFFICER')")
    public ApiResponse<AnomalyRecord> resolveAnomaly(
            @PathVariable Long id,
            @RequestParam String result,
            Authentication authentication) {
        User handler = (User) authentication.getPrincipal();
        return ApiResponse.success(anomalyService.resolveAnomaly(id, result, handler));
    }
}
