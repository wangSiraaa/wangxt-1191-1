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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        if (application.getStatus() != ApplicationStatus.OUTBOUND_COMPLETED
                && application.getStatus() != ApplicationStatus.INBOUND_COMPLETED) {
            throw new BusinessException("申请单状态异常，当前状态: " + application.getStatus() + "，无法回库");
        }

        Explosive explosive = explosiveRepository.findBySerialNo(request.getExplosiveSerialNo())
                .orElseThrow(() -> new BusinessException("器材不存在，编号: " + request.getExplosiveSerialNo()));

        List<OutboundRecord> outboundRecordsByType = outboundRepository.findByApplicationIdAndType(
                application.getId(), explosive.getType());
        List<OutboundRecord> allOutboundRecords = outboundRepository.findByApplicationId(application.getId());

        int totalOutboundBySerial = outboundRecordsByType.stream()
                .filter(r -> r.getExplosiveSerialNo().equals(request.getExplosiveSerialNo()))
                .mapToInt(OutboundRecord::getQuantity)
                .sum();

        if (totalOutboundBySerial == 0) {
            Set<String> validSerials = outboundRecordsByType.stream()
                    .map(OutboundRecord::getExplosiveSerialNo)
                    .collect(Collectors.toSet());
            throw new BusinessException(String.format("器材编号[%s]未从此申请单出库。本申请出库的%s编号: %s",
                    request.getExplosiveSerialNo(),
                    explosive.getType() == ExplosiveType.DETONATOR ? "雷管" : "炸药",
                    validSerials));
        }

        int totalQuantity = request.getUsedQuantity() + request.getReturnedQuantity();
        if (totalQuantity != totalOutboundBySerial) {
            throw new BusinessException(String.format(
                    "器材编号[%s]数量不匹配。出库: %d, 使用: %d + 退回: %d = %d，必须逐项核销完成",
                    request.getExplosiveSerialNo(), totalOutboundBySerial,
                    request.getUsedQuantity(), request.getReturnedQuantity(), totalQuantity));
        }

        int alreadyVerified = inboundRepository.sumVerifiedByApplicationIdAndSerialNo(
                application.getId(), request.getExplosiveSerialNo());
        if (alreadyVerified >= totalOutboundBySerial) {
            throw new BusinessException(String.format("器材编号[%s]已完成核销，无需重复操作", request.getExplosiveSerialNo()));
        }

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

        int totalReturnedDetonators = inboundRepository.sumReturnedQuantityByApplicationIdAndType(
                application.getId(), ExplosiveType.DETONATOR);
        int totalReturnedExplosives = inboundRepository.sumReturnedQuantityByApplicationIdAndType(
                application.getId(), ExplosiveType.EXPLOSIVE);

        int totalUsedDetonators = inboundRepository.sumUsedQuantityByApplicationIdAndType(
                application.getId(), ExplosiveType.DETONATOR);
        int totalUsedExplosives = inboundRepository.sumUsedQuantityByApplicationIdAndType(
                application.getId(), ExplosiveType.EXPLOSIVE);

        int totalOutboundDetonators = outboundRepository.sumQuantityByApplicationIdAndType(
                application.getId(), ExplosiveType.DETONATOR);
        int totalOutboundExplosives = outboundRepository.sumQuantityByApplicationIdAndType(
                application.getId(), ExplosiveType.EXPLOSIVE);

        boolean detonatorsBalanced = (totalReturnedDetonators + totalUsedDetonators) >= totalOutboundDetonators;
        boolean explosivesBalanced = (totalReturnedExplosives + totalUsedExplosives) >= totalOutboundExplosives;

        if (detonatorsBalanced && explosivesBalanced) {
            int unreturnedDetonators = totalOutboundDetonators - totalReturnedDetonators - totalUsedDetonators;
            int unreturnedExplosives = totalOutboundExplosives - totalReturnedExplosives - totalUsedExplosives;

            if (unreturnedDetonators > 0 || unreturnedExplosives > 0) {
                String description = String.format("存在未退回器材，雷管: %d发, 炸药: %dkg",
                        Math.max(0, unreturnedDetonators), Math.max(0, unreturnedExplosives));
                recordAnomaly(application.getShift(), application, AnomalyType.NOT_RETURNED,
                        description, storekeeper);
            }

            Set<String> outboundDetonatorSerials = allOutboundRecords.stream()
                    .filter(r -> r.getType() == ExplosiveType.DETONATOR)
                    .map(OutboundRecord::getExplosiveSerialNo)
                    .collect(Collectors.toSet());
            Set<String> outboundExplosiveSerials = allOutboundRecords.stream()
                    .filter(r -> r.getType() == ExplosiveType.EXPLOSIVE)
                    .map(OutboundRecord::getExplosiveSerialNo)
                    .collect(Collectors.toSet());

            Set<String> inboundDetonatorSerials = new HashSet<>();
            Set<String> inboundExplosiveSerials = new HashSet<>();
            inboundRepository.findByApplicationId(application.getId()).forEach(r -> {
                if (r.getType() == ExplosiveType.DETONATOR) {
                    inboundDetonatorSerials.add(r.getExplosiveSerialNo());
                } else {
                    inboundExplosiveSerials.add(r.getExplosiveSerialNo());
                }
            });

            if (!inboundDetonatorSerials.containsAll(outboundDetonatorSerials)) {
                Set<String> missing = new HashSet<>(outboundDetonatorSerials);
                missing.removeAll(inboundDetonatorSerials);
                throw new BusinessException(String.format("还有雷管编号未完成核销: %s，请逐项核销完成", missing));
            }
            if (!inboundExplosiveSerials.containsAll(outboundExplosiveSerials)) {
                Set<String> missing = new HashSet<>(outboundExplosiveSerials);
                missing.removeAll(inboundExplosiveSerials);
                throw new BusinessException(String.format("还有炸药编号未完成核销: %s，请逐项核销完成", missing));
            }

            application.setStatus(ApplicationStatus.INBOUND_COMPLETED);
            applicationRepository.save(application);

            Shift shift = application.getShift();
            if (totalReturnedDetonators.equals(totalOutboundDetonators - totalUsedDetonators)
                    && totalReturnedExplosives.equals(totalOutboundExplosives - totalUsedExplosives)) {
                shift.setRemainingCleared(true);
            } else {
                shift.setRemainingCleared(false);
            }

            long unresolvedMisfire = anomalyRecordRepository.findUnresolvedByShiftId(shift.getId()).stream()
                    .filter(a -> a.getType() == AnomalyType.MISFIRE)
                    .count();
            shift.setMisfireHandled(unresolvedMisfire == 0);

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
