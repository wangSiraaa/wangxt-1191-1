package com.mine.explosive.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.mine.explosive.dto.ApiResponse;
import com.mine.explosive.entity.Explosive;
import com.mine.explosive.enums.ExplosiveType;
import com.mine.explosive.service.ExplosiveService;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/explosives")

@CrossOrigin(origins = "*")
public class ExplosiveController {

    @Autowired
    public ExplosiveController(ExplosiveService explosiveService) {
        this.explosiveService = explosiveService;
    }

    private final ExplosiveService explosiveService;

    @GetMapping("/{id}")
    public ApiResponse<Explosive> getExplosive(@PathVariable Long id) {
        return ApiResponse.success(explosiveService.getExplosive(id));
    }

    @GetMapping("/serial/{serialNo}")
    public ApiResponse<Explosive> getExplosiveBySerialNo(@PathVariable String serialNo) {
        return ApiResponse.success(explosiveService.getExplosiveBySerialNo(serialNo));
    }

    @GetMapping
    public ApiResponse<List<Explosive>> getExplosives(
            @RequestParam(required = false) ExplosiveType type,
            @RequestParam(required = false, defaultValue = "false") boolean availableOnly) {
        List<Explosive> explosives;
        if (type != null && availableOnly) {
            explosives = explosiveService.getAvailableExplosives(type);
        } else if (type != null) {
            explosives = explosiveService.getExplosivesByType(type);
        } else {
            explosives = explosiveService.getAllExplosives();
        }
        return ApiResponse.success(explosives);
    }

    @GetMapping("/stock")
    public ApiResponse<Map<String, Integer>> getStock() {
        Map<String, Integer> stock = new HashMap<>();
        stock.put("detonators", explosiveService.getAvailableQuantity(ExplosiveType.DETONATOR));
        stock.put("explosives", explosiveService.getAvailableQuantity(ExplosiveType.EXPLOSIVE));
        return ApiResponse.success(stock);
    }
}
