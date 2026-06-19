package com.mine.explosive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.mine.explosive.dto.ApiResponse;
import com.mine.explosive.dto.PickupApplicationRequest;
import com.mine.explosive.dto.ReviewRequest;
import com.mine.explosive.entity.PickupApplication;
import com.mine.explosive.entity.User;
import com.mine.explosive.enums.Role;
import com.mine.explosive.service.PickupApplicationService;
import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")

@CrossOrigin(origins = "*")
public class PickupApplicationController {

    @Autowired
    public PickupApplicationController(PickupApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    private final PickupApplicationService applicationService;

    @PostMapping
    @PreAuthorize("hasRole('BLASTER')")
    public ApiResponse<PickupApplication> createApplication(
            @Valid @RequestBody PickupApplicationRequest request,
            Authentication authentication) {
        User blaster = (User) authentication.getPrincipal();
        return ApiResponse.success(applicationService.createApplication(request, blaster));
    }

    @GetMapping("/{id}")
    public ApiResponse<PickupApplication> getApplication(@PathVariable Long id) {
        return ApiResponse.success(applicationService.getApplication(id));
    }

    @GetMapping
    public ApiResponse<List<PickupApplication>> getApplications(
            @RequestParam(required = false) Long shiftId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<PickupApplication> applications;
        if (shiftId != null) {
            applications = applicationService.getApplicationsByShift(shiftId);
        } else if (user.getRole() == Role.BLASTER) {
            applications = applicationService.getApplicationsByBlaster(user.getId());
        } else {
            applications = applicationService.getAllApplications();
        }
        return ApiResponse.success(applications);
    }

    @GetMapping("/need-review")
    @PreAuthorize("hasAnyRole('SAFETY_OFFICER', 'STOREKEEPER')")
    public ApiResponse<List<PickupApplication>> getApplicationsNeedReview() {
        return ApiResponse.success(applicationService.getApplicationsNeedReview());
    }

    @PostMapping("/review")
    @PreAuthorize("hasAnyRole('SAFETY_OFFICER', 'STOREKEEPER')")
    public ApiResponse<PickupApplication> reviewApplication(
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        User reviewer = (User) authentication.getPrincipal();
        return ApiResponse.success(applicationService.reviewApplication(request, reviewer));
    }
}
