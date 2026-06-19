package com.mine.explosive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.mine.explosive.dto.ApiResponse;
import com.mine.explosive.dto.ShiftRequest;
import com.mine.explosive.entity.Shift;
import com.mine.explosive.entity.User;
import com.mine.explosive.enums.Role;
import com.mine.explosive.enums.ShiftStatus;
import com.mine.explosive.service.ShiftService;
import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shifts")

@CrossOrigin(origins = "*")
public class ShiftController {

    @Autowired
    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    private final ShiftService shiftService;

    @PostMapping
    @PreAuthorize("hasRole('BLASTER')")
    public ApiResponse<Shift> createShift(@Valid @RequestBody ShiftRequest request, Authentication authentication) {
        User blaster = (User) authentication.getPrincipal();
        return ApiResponse.success(shiftService.createShift(request, blaster));
    }

    @GetMapping("/{id}")
    public ApiResponse<Shift> getShift(@PathVariable Long id) {
        return ApiResponse.success(shiftService.getShift(id));
    }

    @GetMapping
    public ApiResponse<List<Shift>> getShifts(
            @RequestParam(required = false) ShiftStatus status,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Shift> shifts;
        if (user.getRole() == Role.BLASTER) {
            shifts = status != null ? shiftService.getShiftsByStatus(status) :
                    shiftService.getShiftsByBlaster(user.getId());
        } else {
            shifts = status != null ? shiftService.getShiftsByStatus(status) :
                    shiftService.getAllShifts();
        }
        return ApiResponse.success(shifts);
    }

    @GetMapping("/my-active")
    @PreAuthorize("hasRole('BLASTER')")
    public ApiResponse<List<Shift>> getMyActiveShifts(Authentication authentication) {
        User blaster = (User) authentication.getPrincipal();
        return ApiResponse.success(shiftService.getActiveShiftsByBlaster(blaster.getId()));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Shift> updateStatus(@PathVariable Long id, @RequestParam ShiftStatus status) {
        return ApiResponse.success(shiftService.updateShiftStatus(id, status));
    }

    @PostMapping("/{id}/close")
    public ApiResponse<Shift> closeShift(@PathVariable Long id, Authentication authentication) {
        User operator = (User) authentication.getPrincipal();
        return ApiResponse.success(shiftService.closeShift(id, operator));
    }
}
