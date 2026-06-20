package com.mine.explosive.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.mine.explosive.dto.ShiftRequest;
import com.mine.explosive.entity.*;
import com.mine.explosive.enums.AnomalyType;
import com.mine.explosive.enums.ApplicationStatus;
import com.mine.explosive.enums.ShiftStatus;
import com.mine.explosive.exception.BusinessException;
import com.mine.explosive.repository.*;
import com.mine.explosive.util.HibernateUtil;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service

public class ShiftService {

    @Autowired
    public ShiftService(ShiftRepository shiftRepository, WorkPlanRepository workPlanRepository,
                        PickupApplicationRepository applicationRepository,
                        VerificationRecordRepository verificationRecordRepository,
                        AnomalyRecordRepository anomalyRecordRepository,
                        HoleChangeRecordRepository holeChangeRepository,
                        OutboundRecordRepository outboundRepository,
                        InboundRecordRepository inboundRepository) {
        this.shiftRepository = shiftRepository;
        this.workPlanRepository = workPlanRepository;
        this.applicationRepository = applicationRepository;
        this.verificationRecordRepository = verificationRecordRepository;
        this.anomalyRecordRepository = anomalyRecordRepository;
        this.holeChangeRepository = holeChangeRepository;
        this.outboundRepository = outboundRepository;
        this.inboundRepository = inboundRepository;
    }

    private final ShiftRepository shiftRepository;
    private final WorkPlanRepository workPlanRepository;
    private final PickupApplicationRepository applicationRepository;
    private final VerificationRecordRepository verificationRecordRepository;
    private final AnomalyRecordRepository anomalyRecordRepository;
    private final HoleChangeRecordRepository holeChangeRepository;
    private final OutboundRecordRepository outboundRepository;
    private final InboundRecordRepository inboundRepository;

    @Transactional
    public Shift createShift(ShiftRequest request, User blaster) {
        if (shiftRepository.existsByBlasterIdAndStatusNot(blaster.getId(), ShiftStatus.CLOSED)) {
            throw new BusinessException("您存在未关闭的当班作业，请先关闭后再创建新作业");
        }

        if (request.getWorkPlanId() == null) {
            throw new BusinessException("请选择作业计划");
        }

        WorkPlan workPlan = workPlanRepository.findById(request.getWorkPlanId())
                .orElseThrow(() -> new BusinessException("作业计划不存在"));

        Shift shift = new Shift();
        shift.setShiftNo("SHIFT" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        shift.setWorkFace(workPlan.getWorkFace());
        shift.setWorkPlan(workPlan);
        shift.setBlaster(blaster);
        shift.setStatus(ShiftStatus.OPEN);
        shift.setStartTime(LocalDateTime.now());
        shift.setRemarks(request.getRemarks());
        shift.setActualHoles(workPlan.getDesignedHoles());
        shift.setRemainingCleared(false);
        shift.setMisfireHandled(true);

        return shiftRepository.save(shift);
    }

    public Shift getShift(Long id) {
        Shift shift = shiftRepository.findById(id)
                .orElseThrow(() -> new BusinessException("当班作业不存在"));
        HibernateUtil.initShift(shift);
        return shift;
    }

    public List<Shift> getShiftsByBlaster(Long blasterId) {
        List<Shift> shifts = shiftRepository.findByBlasterId(blasterId);
        shifts.forEach(HibernateUtil::initShift);
        return shifts;
    }

    public List<Shift> getActiveShiftsByBlaster(Long blasterId) {
        List<Shift> shifts = shiftRepository.findActiveShiftsByBlasterId(blasterId);
        shifts.forEach(HibernateUtil::initShift);
        return shifts;
    }

    public List<Shift> getAllShifts() {
        List<Shift> shifts = shiftRepository.findAll();
        shifts.forEach(HibernateUtil::initShift);
        return shifts;
    }

    public List<Shift> getShiftsByStatus(ShiftStatus status) {
        List<Shift> shifts = shiftRepository.findByStatus(status);
        shifts.forEach(HibernateUtil::initShift);
        return shifts;
    }

    @Transactional
    public Shift updateShiftStatus(Long shiftId, ShiftStatus status) {
        Shift shift = getShift(shiftId);
        shift.setStatus(status);
        return shiftRepository.save(shift);
    }

    @Transactional
    public Shift closeShift(Long shiftId, User operator) {
        Shift shift = getShift(shiftId);

        if (applicationRepository.existsActiveApplicationByShiftId(shiftId)) {
            throw new BusinessException("存在未完成的领用申请，无法关闭当班作业");
        }

        List<ApplicationStatus> incompleteStatuses = List.of(
                ApplicationStatus.PENDING,
                ApplicationStatus.APPROVED,
                ApplicationStatus.NEED_REVIEW,
                ApplicationStatus.OUTBOUND_COMPLETED
        );

        List<PickupApplication> incompleteApps = applicationRepository
                .findByShiftIdAndStatusIn(shiftId, incompleteStatuses);
        if (!incompleteApps.isEmpty()) {
            throw new BusinessException("存在未完成回库的申请，无法关闭当班作业");
        }

        if (holeChangeRepository.existsPendingByShiftId(shiftId)) {
            List<HoleChangeRecord> pendingHoleChanges = holeChangeRepository.findPendingByShiftId(shiftId);
            String pendingNos = pendingHoleChanges.stream()
                    .map(HoleChangeRecord::getChangeNo)
                    .collect(Collectors.toList()).toString();
            throw new BusinessException(String.format("存在待复核的孔数变更申请: %s，请先完成孔数变更复核后再关闭当班作业", pendingNos));
        }

        List<AnomalyRecord> unresolvedMisfires = anomalyRecordRepository.findUnresolvedByShiftId(shiftId).stream()
                .filter(a -> a.getType() == AnomalyType.MISFIRE)
                .collect(Collectors.toList());

        if (!unresolvedMisfires.isEmpty()) {
            String misfireNos = unresolvedMisfires.stream()
                    .map(AnomalyRecord::getRecordNo)
                    .collect(Collectors.toList()).toString();
            shift.setMisfireHandled(false);
            shiftRepository.save(shift);
            throw new BusinessException(String.format("存在未闭环的哑炮处置记录: %s，哑炮处置未闭环，当班作业不能关闭", misfireNos));
        }
        shift.setMisfireHandled(true);

        List<PickupApplication> allApps = applicationRepository.findByShiftId(shiftId);
        if (allApps.isEmpty()) {
            shift.setRemainingCleared(true);
        } else {
            int totalOutboundDetonators = 0;
            int totalOutboundExplosives = 0;
            int totalReturnedDetonators = 0;
            int totalReturnedExplosives = 0;
            int totalUsedDetonators = 0;
            int totalUsedExplosives = 0;

            for (PickupApplication app : allApps) {
                totalOutboundDetonators += safeInt(outboundRepository.sumQuantityByApplicationIdAndType(
                        app.getId(), com.mine.explosive.enums.ExplosiveType.DETONATOR));
                totalOutboundExplosives += safeInt(outboundRepository.sumQuantityByApplicationIdAndType(
                        app.getId(), com.mine.explosive.enums.ExplosiveType.EXPLOSIVE));
                totalReturnedDetonators += safeInt(inboundRepository.sumReturnedQuantityByApplicationIdAndType(
                        app.getId(), com.mine.explosive.enums.ExplosiveType.DETONATOR));
                totalReturnedExplosives += safeInt(inboundRepository.sumReturnedQuantityByApplicationIdAndType(
                        app.getId(), com.mine.explosive.enums.ExplosiveType.EXPLOSIVE));
                totalUsedDetonators += safeInt(inboundRepository.sumUsedQuantityByApplicationIdAndType(
                        app.getId(), com.mine.explosive.enums.ExplosiveType.DETONATOR));
                totalUsedExplosives += safeInt(inboundRepository.sumUsedQuantityByApplicationIdAndType(
                        app.getId(), com.mine.explosive.enums.ExplosiveType.EXPLOSIVE));
            }

            int remainingDetonators = totalOutboundDetonators - totalReturnedDetonators - totalUsedDetonators;
            int remainingExplosives = totalOutboundExplosives - totalReturnedExplosives - totalUsedExplosives;

            if (remainingDetonators != 0 || remainingExplosives != 0) {
                shift.setRemainingCleared(false);
                shiftRepository.save(shift);
                throw new BusinessException(String.format(
                        "剩余器材未清零：雷管剩余%d发，炸药剩余%dkg，请完成剩余器材退回或核销后再关闭当班作业",
                        Math.max(0, remainingDetonators), Math.max(0, remainingExplosives)));
            }
            shift.setRemainingCleared(true);
        }

        boolean hasVerification = false;
        for (PickupApplication app : allApps) {
            if (verificationRecordRepository.existsByApplicationId(app.getId())) {
                hasVerification = true;
                break;
            }
        }
        if (!hasVerification && !allApps.isEmpty()) {
            throw new BusinessException("存在未经过安全负责人核对的申请，无法关闭当班作业");
        }

        shift.setStatus(ShiftStatus.CLOSED);
        shift.setEndTime(LocalDateTime.now());
        return shiftRepository.save(shift);
    }

    private int safeInt(Integer value) {
        return value != null ? value : 0;
    }
}
