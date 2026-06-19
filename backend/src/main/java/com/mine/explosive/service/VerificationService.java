package com.mine.explosive.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.mine.explosive.dto.VerificationRequest;
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

public class VerificationService {

    @Autowired
    public VerificationService(VerificationRecordRepository verificationRepository, PickupApplicationRepository applicationRepository, OutboundRecordRepository outboundRepository, InboundRecordRepository inboundRepository, AnomalyRecordRepository anomalyRecordRepository, ShiftRepository shiftRepository) {
        this.verificationRepository = verificationRepository;
        this.applicationRepository = applicationRepository;
        this.outboundRepository = outboundRepository;
        this.inboundRepository = inboundRepository;
        this.anomalyRecordRepository = anomalyRecordRepository;
        this.shiftRepository = shiftRepository;
    }

    private final VerificationRecordRepository verificationRepository;
    private final PickupApplicationRepository applicationRepository;
    private final OutboundRecordRepository outboundRepository;
    private final InboundRecordRepository inboundRepository;
    private final AnomalyRecordRepository anomalyRecordRepository;
    private final ShiftRepository shiftRepository;

    @Transactional
    public VerificationRecord createVerification(VerificationRequest request, User safetyOfficer) {
        if (safetyOfficer.getRole() != Role.SAFETY_OFFICER) {
            throw new BusinessException("只有安全负责人可以执行核对操作");
        }

        PickupApplication application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new BusinessException("申请单不存在"));

        if (application.getStatus() != ApplicationStatus.INBOUND_COMPLETED) {
            throw new BusinessException("申请单未完成回库，无法核对");
        }

        Shift shift = application.getShift();

        int outboundDetonators = outboundRepository.sumQuantityByApplicationIdAndType(
                application.getId(), ExplosiveType.DETONATOR);
        int outboundExplosives = outboundRepository.sumQuantityByApplicationIdAndType(
                application.getId(), ExplosiveType.EXPLOSIVE);

        int returnedDetonators = inboundRepository.sumReturnedQuantityByApplicationIdAndType(
                application.getId(), ExplosiveType.DETONATOR);
        int returnedExplosives = inboundRepository.sumReturnedQuantityByApplicationIdAndType(
                application.getId(), ExplosiveType.EXPLOSIVE);

        int usedDetonators = inboundRepository.sumUsedQuantityByApplicationIdAndType(
                application.getId(), ExplosiveType.DETONATOR);
        int usedExplosives = inboundRepository.sumUsedQuantityByApplicationIdAndType(
                application.getId(), ExplosiveType.EXPLOSIVE);

        int totalDetonators = usedDetonators + returnedDetonators;
        int totalExplosives = usedExplosives + returnedExplosives;

        boolean allReturned = (request.getUsedDetonators() + request.getReturnedDetonators() >= outboundDetonators) &&
                (request.getUsedExplosives() + request.getReturnedExplosives() >= outboundExplosives) &&
                (request.getReturnedDetonators() + request.getUsedDetonators() >= totalDetonators) &&
                (request.getReturnedExplosives() + request.getUsedExplosives() >= totalExplosives);

        if (!allReturned) {
            int unreturnedDetonators = outboundDetonators - request.getUsedDetonators() - request.getReturnedDetonators();
            int unreturnedExplosives = outboundExplosives - request.getUsedExplosives() - request.getReturnedExplosives();

            if (unreturnedDetonators > 0 || unreturnedExplosives > 0) {
                String description = String.format("安全负责人核对发现未退回器材，雷管: %d发, 炸药: %dkg",
                        Math.max(0, unreturnedDetonators), Math.max(0, unreturnedExplosives));
                recordAnomaly(shift, application, AnomalyType.NOT_RETURNED, description, safetyOfficer);
                throw new BusinessException(description + "，请确认所有器材已退回或记录使用情况");
            }
        }

        VerificationRecord record = new VerificationRecord();
        record.setVerificationNo("VER" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        record.setShift(shift);
        record.setApplication(application);
        record.setSafetyOfficer(safetyOfficer);
        record.setExpectedDetonators(request.getExpectedDetonators() != null ? request.getExpectedDetonators() : outboundDetonators);
        record.setExpectedExplosives(request.getExpectedExplosives() != null ? request.getExpectedExplosives() : outboundExplosives);
        record.setUsedDetonators(request.getUsedDetonators());
        record.setReturnedDetonators(request.getReturnedDetonators());
        record.setUsedExplosives(request.getUsedExplosives());
        record.setReturnedExplosives(request.getReturnedExplosives());
        record.setAllReturned(allReturned);
        record.setVerificationRemark(request.getVerificationRemark());
        record.setVerificationTime(LocalDateTime.now());

        application.setStatus(ApplicationStatus.CLOSED);
        applicationRepository.save(application);

        boolean allApplicationsVerified = applicationRepository.findByShiftId(shift.getId()).stream()
                .allMatch(app -> app.getStatus() == ApplicationStatus.CLOSED);

        if (allApplicationsVerified) {
            shift.setStatus(ShiftStatus.CLOSED);
            shift.setEndTime(LocalDateTime.now());
            shiftRepository.save(shift);
        }

        return verificationRepository.save(record);
    }

    public VerificationRecord getVerification(Long id) {
        return verificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("核对记录不存在"));
    }

    public List<VerificationRecord> getVerificationsByShift(Long shiftId) {
        return verificationRepository.findByShiftId(shiftId);
    }

    public List<VerificationRecord> getVerificationsByApplication(Long applicationId) {
        return verificationRepository.findByApplicationId(applicationId);
    }

    public List<VerificationRecord> getAllVerifications() {
        return verificationRepository.findAll();
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
