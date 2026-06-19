package com.mine.explosive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.mine.explosive.dto.ApiResponse;
import com.mine.explosive.dto.OutboundRequest;
import com.mine.explosive.entity.OutboundRecord;
import com.mine.explosive.entity.User;
import com.mine.explosive.enums.Role;
import com.mine.explosive.service.OutboundService;
import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/outbound")

@CrossOrigin(origins = "*")
public class OutboundController {

    @Autowired
    public OutboundController(OutboundService outboundService) {
        this.outboundService = outboundService;
    }

    private final OutboundService outboundService;

    @PostMapping
    @PreAuthorize("hasRole('STOREKEEPER')")
    public ApiResponse<OutboundRecord> createOutbound(
            @Valid @RequestBody OutboundRequest request,
            Authentication authentication) {
        User storekeeper = (User) authentication.getPrincipal();
        return ApiResponse.success(outboundService.createOutbound(request, storekeeper));
    }

    @GetMapping("/{id}")
    public ApiResponse<OutboundRecord> getOutbound(@PathVariable Long id) {
        return ApiResponse.success(outboundService.getOutbound(id));
    }

    @GetMapping
    public ApiResponse<List<OutboundRecord>> getOutbounds(
            @RequestParam(required = false) Long applicationId,
            @RequestParam(required = false) String serialNo,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<OutboundRecord> records;
        if (applicationId != null) {
            records = outboundService.getOutboundsByApplication(applicationId);
        } else if (serialNo != null) {
            records = outboundService.getOutboundsByExplosive(serialNo);
        } else if (user.getRole() == Role.BLASTER) {
            records = outboundService.getOutboundsByBlaster(user.getId());
        } else {
            records = outboundService.getAllOutbounds();
        }
        return ApiResponse.success(records);
    }
}
