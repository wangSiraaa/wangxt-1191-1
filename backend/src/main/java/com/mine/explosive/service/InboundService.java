package com.mine.explosive.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.mine.explosive.dto.InboundRequest;
import com.mine.explosive.entity.*;
import com.mine.explosive.enums.AnomalyType;
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

public class InboundService {

    @Autowired
    public InboundService(InboundRecordRepository inboundRepository, OutboundRecordRepository outboundRepository, PickupApplicationRepository applicationRepository, ExplosiveRepository explosiveRepository, AnomalyRecordRepository anomalyRecordRepository, ShiftRepository shiftRepository) {
        this.inboundRepository = inboundRepository;
        this.outboundRepository = outboundRepository;
        this.applicationRepository = applicationRepository;
        this.explosiveRepository = explosiveRepository;
        this.anomalyRecordRepository = anomalyRecordRepository;
        this.shiftRepository = shiftRepository;
    }

    private final InboundRecordRepository inboundRepository;
    private final OutboundRecordRepository outboundRepository;
    private final PickupApplicationRepository applicationRepository;
    private final ExplosiveRepository explosiveRepository;
    private final AnomalyRecordRepository anomalyRecordRepository;
    private final ShiftRepository shiftRepository;

    @Transactional
    public InboundRecord createInbound(InboundRequest request, User storekeeper) {
        if (storekeeper.getRole() != Role.STOREKEEPER) {
            throw new BusinessException("只有库管可以执行回库操作");
        }

        PickupApplication application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new BusinessException("申请单不存在"));

        if (application.getStatus() != ApplicationStatus.OUTBOUND_COMPLETED) {
            throw new BusinessException("申请单未完成出库，无法回库");
        }

        Explosive explosive = explosiveRepository.findBySerialNo(request.getExplosiveSerialNo())
                .orElseThrow(() -> new BusinessException("器材不存在，编号: " + request.getExplosiveSerialNo()));

        List<OutboundRecord> outboundRecords = outboundRepository.findByApplicationIdAndType(
                application.getId(), explosive.getType());

        int totalOutbound = outboundRecords.stream()
                .filter(r -> r.getExplosiveSerialNo().equals(request.getExplosiveSerialNo()))
                .mapToInt(OutboundRecord::getQuantity)
                .sum();

        if (totalOutbound == 0) {
            throw new BusinessException("该器材未从此申请单出库");
        }

        int totalQuantity = request.getUsedQuantity() + request.getReturnedQuantity();
        if (totalQuantity != totalOutbound) {
            throw new BusinessException(String.format("数量不匹配。出库: %d, 使用+退回: %d",
                    totalOutbound, totalQuantity));
        }

        int alreadyReturned = inboundRepository.sumReturnedQuantityByApplicationIdAndType(
                application.getId(), explosive.getType());
        int alreadyUsed = inboundRepository.sumUsedQuantityByApplicationIdAndType(
                application.getId(), explosive.getType());

        InboundRecord record = new InboundRecord();
        record.setInboundNo("IN" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        record.setApplication(application);
        record.setExplosive(explosive);
        record.setType(explosive.getType());
        record.setExplosiveSerialNo(explosive.getSerialNo());
        record.setUsedQuantity(request.getUsedQuantity());
        record.setReturnedQuantity(request.getReturnedQuantity());
        record.setStorekeeper(storekeeper);
        record.setBlaster(application.getBlaster());
        record.setInboundTime(LocalDateTime.now());
        record.setRemarks(request.getRemarks());

        if (request.getReturnedQuantity() > 0) {
            explosive.setAvailableQuantity(explosive.getAvailableQuantity() + request.getReturnedQuantity());
            explosiveRepository.save(explosive);
        }

        inboundRepository.save(record);

        int totalReturnedDetonators = alreadyReturned +
                (explosive.getType() == ExplosiveType.DETONATOR ? request.getReturnedQuantity() : 0);
        int totalReturnedExplosives = alreadyReturned +
                (explosive.getType() == ExplosiveType.EXPLOSIVE ? request.getReturnedQuantity() : 0);

        int totalUsedDetonators = alreadyUsed +
                (explosive.getType() == ExplosiveType.DETONATOR ? request.getUsedQuantity() : 0);
        int totalUsedExplosives = alreadyUsed +
                (explosive.getType() == ExplosiveType.EXPLOSIVE ? request.getUsedQuantity() : 0);

        int totalOutboundDetonators = outboundRepository.sumQuantityByApplicationIdAndType(
                application.getId(), ExplosiveType.DETONATOR);
        int totalOutboundExplosives = outboundRepository.sumQuantityByApplicationIdAndType(
                application.getId(), ExplosiveType.EXPLOSIVE);

        if (totalReturnedDetonators + totalUsedDetonators >= totalOutboundDetonators &&
                totalReturnedExplosives + totalUsedExplosives >= totalOutboundExplosives) {

            if (totalReturnedDetonators < totalOutboundDetonators ||
                    totalReturnedExplosives < totalOutboundExplosives) {
                int unreturnedDetonators = totalOutboundDetonators - totalReturnedDetonators - totalUsedDetonators;
                int unreturnedExplosives = totalOutboundExplosives - totalReturnedExplosives - totalUsedExplosives;

                if (unreturnedDetonators > 0 || unreturnedExplosives > 0) {
                    String description = String.format("存在未退回器材，雷管: %d发, 炸药: %dkg",
                            Math.max(0, unreturnedDetonators), Math.max(0, unreturnedExplosives));
                    recordAnomaly(application.getShift(), application, AnomalyType.NOT_RETURNED,
                            description, storekeeper);
                }
            }

            application.setStatus(ApplicationStatus.INBOUND_COMPLETED);
            applicationRepository.save(application);

            Shift shift = application.getShift();
            shift.setStatus(ShiftStatus.WAITING_VERIFY);
            shiftRepository.save(shift);
        }

        return record;
    }

    public InboundRecord getInbound(Long id) {
        return inboundRepository.findById(id)
                .orElseThrow(() -> new BusinessException("回库记录不存在"));
    }

    public List<InboundRecord> getInboundsByApplication(Long applicationId) {
        return inboundRepository.findByApplicationId(applicationId);
    }

    public List<InboundRecord> getInboundsByBlaster(Long blasterId) {
        return inboundRepository.findByBlasterId(blasterId);
    }

    public List<InboundRecord> getAllInbounds() {
        return inboundRepository.findAll();
    }

    private void recordAnomaly(Shift shift, PickupApplication application, AnomalyType type,
                               String description, User reporter) {
        AnomalyRecord anomaly = new AnomalyRecord();
        anomaly.setRecordNo("ANOMALY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        anomaly.setShift(shift);
        anomaly.setApplication(application);
        anomaly.setType(type);
        anomaly.setDescription(description);
        anomaly.setReportedBy(reporter);
        anomaly.setResolved(false);
        anomalyRecordRepository.save(anomaly);
    }
}
