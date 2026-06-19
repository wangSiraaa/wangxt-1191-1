package com.mine.explosive.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.mine.explosive.dto.OutboundRequest;
import com.mine.explosive.entity.*;
import com.mine.explosive.enums.ApplicationStatus;
import com.mine.explosive.enums.ExplosiveType;
import com.mine.explosive.enums.Role;
import com.mine.explosive.enums.ShiftStatus;
import com.mine.explosive.exception.BusinessException;
import com.mine.explosive.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service

public class OutboundService {

    @Autowired
    public OutboundService(OutboundRecordRepository outboundRepository, PickupApplicationRepository applicationRepository, ExplosiveRepository explosiveRepository, ShiftRepository shiftRepository) {
        this.outboundRepository = outboundRepository;
        this.applicationRepository = applicationRepository;
        this.explosiveRepository = explosiveRepository;
        this.shiftRepository = shiftRepository;
    }

    private final OutboundRecordRepository outboundRepository;
    private final PickupApplicationRepository applicationRepository;
    private final ExplosiveRepository explosiveRepository;
    private final ShiftRepository shiftRepository;

    @Transactional
    public OutboundRecord createOutbound(OutboundRequest request, User storekeeper) {
        if (storekeeper.getRole() != Role.STOREKEEPER) {
            throw new BusinessException("只有库管可以执行出库操作");
        }

        PickupApplication application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new BusinessException("申请单不存在"));

        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new BusinessException("申请单未批准，无法出库");
        }

        Explosive explosive = explosiveRepository.findBySerialNo(request.getExplosiveSerialNo())
                .orElseThrow(() -> new BusinessException("器材不存在，编号: " + request.getExplosiveSerialNo()));

        if (explosive.getAvailableQuantity() < request.getQuantity()) {
            throw new BusinessException("器材库存不足，现有库存: " + explosive.getAvailableQuantity());
        }

        Integer alreadyOutbound = outboundRepository.sumQuantityByApplicationIdAndType(
                application.getId(), explosive.getType());

        int maxAllowed = explosive.getType() == ExplosiveType.DETONATOR
                ? application.getDetonatorQuantity()
                : application.getExplosiveQuantity();

        if (alreadyOutbound + request.getQuantity() > maxAllowed) {
            throw new BusinessException(String.format("出库数量超出申请数量，申请: %d, 已出库: %d, 本次: %d",
                    maxAllowed, alreadyOutbound, request.getQuantity()));
        }

        OutboundRecord record = new OutboundRecord();
        record.setOutboundNo("OUT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        record.setApplication(application);
        record.setExplosive(explosive);
        record.setType(explosive.getType());
        record.setExplosiveSerialNo(explosive.getSerialNo());
        record.setQuantity(request.getQuantity());
        record.setStorekeeper(storekeeper);
        record.setBlaster(application.getBlaster());
        record.setOutboundTime(LocalDateTime.now());
        record.setWorkFace(application.getShift().getWorkFace());

        explosive.setAvailableQuantity(explosive.getAvailableQuantity() - request.getQuantity());
        explosiveRepository.save(explosive);

        int newOutboundDetonators = outboundRepository.sumQuantityByApplicationIdAndType(
                application.getId(), ExplosiveType.DETONATOR) +
                (explosive.getType() == ExplosiveType.DETONATOR ? request.getQuantity() : 0);
        int newOutboundExplosives = outboundRepository.sumQuantityByApplicationIdAndType(
                application.getId(), ExplosiveType.EXPLOSIVE) +
                (explosive.getType() == ExplosiveType.EXPLOSIVE ? request.getQuantity() : 0);

        if (newOutboundDetonators >= application.getDetonatorQuantity() &&
                newOutboundExplosives >= application.getExplosiveQuantity()) {
            application.setStatus(ApplicationStatus.OUTBOUND_COMPLETED);
            applicationRepository.save(application);

            Shift shift = application.getShift();
            shift.setStatus(ShiftStatus.WAITING_RETURN);
            shiftRepository.save(shift);
        }

        return outboundRepository.save(record);
    }

    public OutboundRecord getOutbound(Long id) {
        return outboundRepository.findById(id)
                .orElseThrow(() -> new BusinessException("出库记录不存在"));
    }

    public List<OutboundRecord> getOutboundsByApplication(Long applicationId) {
        return outboundRepository.findByApplicationId(applicationId);
    }

    public List<OutboundRecord> getOutboundsByBlaster(Long blasterId) {
        return outboundRepository.findByBlasterId(blasterId);
    }

    public List<OutboundRecord> getAllOutbounds() {
        return outboundRepository.findAll();
    }

    public List<OutboundRecord> getOutboundsByExplosive(String serialNo) {
        return outboundRepository.findByExplosiveSerialNo(serialNo);
    }
}
