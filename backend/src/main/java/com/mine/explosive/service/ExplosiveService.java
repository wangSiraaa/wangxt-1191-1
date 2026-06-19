package com.mine.explosive.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.mine.explosive.entity.Explosive;
import com.mine.explosive.enums.ExplosiveType;
import com.mine.explosive.exception.BusinessException;
import com.mine.explosive.repository.ExplosiveRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class ExplosiveService {

    @Autowired
    public ExplosiveService(ExplosiveRepository explosiveRepository) {
        this.explosiveRepository = explosiveRepository;
    }

    private final ExplosiveRepository explosiveRepository;

    public Explosive getExplosive(Long id) {
        return explosiveRepository.findById(id)
                .orElseThrow(() -> new BusinessException("器材不存在"));
    }

    public Explosive getExplosiveBySerialNo(String serialNo) {
        return explosiveRepository.findBySerialNo(serialNo)
                .orElseThrow(() -> new BusinessException("器材不存在，编号: " + serialNo));
    }

    public List<Explosive> getAllExplosives() {
        return explosiveRepository.findAll();
    }

    public List<Explosive> getExplosivesByType(ExplosiveType type) {
        return explosiveRepository.findByType(type);
    }

    public List<Explosive> getAvailableExplosives(ExplosiveType type) {
        return explosiveRepository.findAvailableByType(type);
    }

    public Integer getAvailableQuantity(ExplosiveType type) {
        Integer quantity = explosiveRepository.sumAvailableQuantityByType(type);
        return quantity != null ? quantity : 0;
    }
}
