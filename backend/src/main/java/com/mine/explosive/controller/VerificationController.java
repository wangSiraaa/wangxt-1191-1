package com.mine.explosive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.mine.explosive.dto.ApiResponse;
import com.mine.explosive.dto.VerificationRequest;
import com.mine.explosive.entity.VerificationRecord;
import com.mine.explosive.entity.User;
import com.mine.explosive.service.VerificationService;
import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/verification")

@CrossOrigin(origins = "*")
public class VerificationController {

    @Autowired
    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    private final VerificationService verificationService;

    @PostMapping
    @PreAuthorize("hasRole('SAFETY_OFFICER')")
    public ApiResponse<VerificationRecord> createVerification(
            @Valid @RequestBody VerificationRequest request,
            Authentication authentication) {
        User safetyOfficer = (User) authentication.getPrincipal();
        return ApiResponse.success(verificationService.createVerification(request, safetyOfficer));
    }

    @GetMapping("/{id}")
    public ApiResponse<VerificationRecord> getVerification(@PathVariable Long id) {
        return ApiResponse.success(verificationService.getVerification(id));
    }

    @GetMapping
    public ApiResponse<List<VerificationRecord>> getVerifications(
            @RequestParam(required = false) Long shiftId,
            @RequestParam(required = false) Long applicationId) {
        List<VerificationRecord> records;
        if (shiftId != null) {
            records = verificationService.getVerificationsByShift(shiftId);
        } else if (applicationId != null) {
            records = verificationService.getVerificationsByApplication(applicationId);
        } else {
            records = verificationService.getAllVerifications();
        }
        return ApiResponse.success(records);
    }
}
