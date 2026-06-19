package com.mine.explosive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.mine.explosive.dto.ApiResponse;
import com.mine.explosive.dto.InboundRequest;
import com.mine.explosive.entity.InboundRecord;
import com.mine.explosive.entity.User;
import com.mine.explosive.enums.Role;
import com.mine.explosive.service.InboundService;
import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inbound")

@CrossOrigin(origins = "*")
public class InboundController {

    @Autowired
    public InboundController(InboundService inboundService) {
        this.inboundService = inboundService;
    }

    private final InboundService inboundService;

    @PostMapping
    @PreAuthorize("hasRole('STOREKEEPER')")
    public ApiResponse<InboundRecord> createInbound(
            @Valid @RequestBody InboundRequest request,
            Authentication authentication) {
        User storekeeper = (User) authentication.getPrincipal();
        return ApiResponse.success(inboundService.createInbound(request, storekeeper));
    }

    @GetMapping("/{id}")
    public ApiResponse<InboundRecord> getInbound(@PathVariable Long id) {
        return ApiResponse.success(inboundService.getInbound(id));
    }

    @GetMapping
    public ApiResponse<List<InboundRecord>> getInbounds(
            @RequestParam(required = false) Long applicationId,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<InboundRecord> records;
        if (applicationId != null) {
            records = inboundService.getInboundsByApplication(applicationId);
        } else if (user.getRole() == Role.BLASTER) {
            records = inboundService.getInboundsByBlaster(user.getId());
        } else {
            records = inboundService.getAllInbounds();
        }
        return ApiResponse.success(records);
    }
}
