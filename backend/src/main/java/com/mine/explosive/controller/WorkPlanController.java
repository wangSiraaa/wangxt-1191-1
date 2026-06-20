package com.mine.explosive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import com.mine.explosive.dto.ApiResponse;
import com.mine.explosive.entity.WorkPlan;
import com.mine.explosive.entity.User;
import com.mine.explosive.service.WorkPlanService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-plans")
public class WorkPlanController {

    private final WorkPlanService workPlanService;

    @Autowired
    public WorkPlanController(WorkPlanService workPlanService) {
        this.workPlanService = workPlanService;
    }

    @GetMapping
    public ApiResponse<List<WorkPlan>> getAllWorkPlans() {
        return ApiResponse.success(workPlanService.getAllWorkPlans());
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkPlan> getWorkPlanById(@PathVariable Long id) {
        return ApiResponse.success(workPlanService.getWorkPlan(id));
    }

    @GetMapping("/my")
    public ApiResponse<List<WorkPlan>> getMyWorkPlans(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ApiResponse.success(workPlanService.getMyWorkPlans(user.getId()));
    }
}
