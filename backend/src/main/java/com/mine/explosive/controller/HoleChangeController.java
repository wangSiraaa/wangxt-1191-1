package com.mine.explosive.controller;

import com.mine.explosive.annotation.RequiredRole;
import com.mine.explosive.dto.HoleChangeRequest;
import com.mine.explosive.dto.ReviewRequest;
import com.mine.explosive.entity.HoleChangeRecord;
import com.mine.explosive.entity.User;
import com.mine.explosive.enums.Role;
import com.mine.explosive.security.util.SecurityUtil;
import com.mine.explosive.service.HoleChangeService;
import com.mine.explosive.util.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hole-changes")
public class HoleChangeController {

    @Autowired
    public HoleChangeController(HoleChangeService holeChangeService, SecurityUtil securityUtil) {
        this.holeChangeService = holeChangeService;
        this.securityUtil = securityUtil;
    }

    private final HoleChangeService holeChangeService;
    private final SecurityUtil securityUtil;

    @PostMapping
    @RequiredRole({Role.BLASTER, Role.SAFETY_OFFICER})
    public ResponseEntity<ApiResponse<HoleChangeRecord>> createHoleChange(
            @Valid @RequestBody HoleChangeRequest request) {
        User currentUser = securityUtil.getCurrentUser();
        HoleChangeRecord record = holeChangeService.createHoleChange(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(record));
    }

    @PostMapping("/{id}/review")
    @RequiredRole({Role.SAFETY_OFFICER})
    public ResponseEntity<ApiResponse<HoleChangeRecord>> reviewHoleChange(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request) {
        User currentUser = securityUtil.getCurrentUser();
        request.setId(id);
        HoleChangeRecord record = holeChangeService.reviewHoleChange(request, currentUser);
        return ResponseEntity.ok(ApiResponse.success(record));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HoleChangeRecord>> getHoleChange(@PathVariable Long id) {
        HoleChangeRecord record = holeChangeService.getHoleChange(id);
        return ResponseEntity.ok(ApiResponse.success(record));
    }

    @GetMapping("/shift/{shiftId}")
    public ResponseEntity<ApiResponse<List<HoleChangeRecord>>> getHoleChangesByShift(
            @PathVariable Long shiftId) {
        List<HoleChangeRecord> records = holeChangeService.getHoleChangesByShift(shiftId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/shift/{shiftId}/pending")
    public ResponseEntity<ApiResponse<List<HoleChangeRecord>>> getPendingByShift(
            @PathVariable Long shiftId) {
        List<HoleChangeRecord> records = holeChangeService.getPendingByShift(shiftId);
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping("/need-review")
    @RequiredRole({Role.SAFETY_OFFICER, Role.STOREKEEPER})
    public ResponseEntity<ApiResponse<List<HoleChangeRecord>>> getHoleChangesNeedReview() {
        List<HoleChangeRecord> records = holeChangeService.getHoleChangesNeedReview();
        return ResponseEntity.ok(ApiResponse.success(records));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<HoleChangeRecord>>> getAllHoleChanges() {
        List<HoleChangeRecord> records = holeChangeService.getAllHoleChanges();
        return ResponseEntity.ok(ApiResponse.success(records));
    }
}
