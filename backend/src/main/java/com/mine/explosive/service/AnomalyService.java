package com.mine.explosive.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.mine.explosive.dto.AnomalyRequest;
import com.mine.explosive.entity.AnomalyRecord;
import com.mine.explosive.entity.Shift;
import com.mine.explosive.entity.User;
import com.mine.explosive.enums.Role;
import com.mine.explosive.exception.BusinessException;
import com.mine.explosive.repository.AnomalyRecordRepository;
import com.mine.explosive.repository.PickupApplicationRepository;
import com.mine.explosive.repository.ShiftRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service

public class AnomalyService {

    @Autowired
    public AnomalyService(AnomalyRecordRepository anomalyRepository, ShiftRepository shiftRepository, PickupApplicationRepository applicationRepository) {
        this.anomalyRepository = anomalyRepository;
        this.shiftRepository = shiftRepository;
        this.applicationRepository = applicationRepository;
    }

    private final AnomalyRecordRepository anomalyRepository;
    private final ShiftRepository shiftRepository;
    private final PickupApplicationRepository applicationRepository;

    @Transactional
    public AnomalyRecord createAnomaly(AnomalyRequest request, User reporter) {
        AnomalyRecord anomaly = new AnomalyRecord();
        anomaly.setRecordNo("ANOMALY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        anomaly.setType(request.getType());
        anomaly.setDescription(request.getDescription());
        anomaly.setExplosiveSerialNo(request.getExplosiveSerialNo());
        anomaly.setAnomalyQuantity(request.getAnomalyQuantity());
        anomaly.setReportedBy(reporter);
        anomaly.setResolved(false);

        if (request.getShiftId() != null) {
            Shift shift = shiftRepository.findById(request.getShiftId())
                    .orElseThrow(() -> new BusinessException("当班作业不存在"));
            anomaly.setShift(shift);
        }

        if (request.getApplicationId() != null) {
            anomaly.setApplication(applicationRepository.findById(request.getApplicationId())
                    .orElseThrow(() -> new BusinessException("申请单不存在")));
        }

        return anomalyRepository.save(anomaly);
    }

    @Transactional
    public AnomalyRecord resolveAnomaly(Long id, String result, User handler) {
        if (handler.getRole() != Role.SAFETY_OFFICER) {
            throw new BusinessException("只有安全负责人可以处理异常");
        }

        AnomalyRecord anomaly = anomalyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("异常记录不存在"));

        anomaly.setResolved(true);
        anomaly.setHandledBy(handler);
        anomaly.setHandledAt(LocalDateTime.now());
        anomaly.setHandlingResult(result);

        return anomalyRepository.save(anomaly);
    }

    public AnomalyRecord getAnomaly(Long id) {
        return anomalyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("异常记录不存在"));
    }

    public List<AnomalyRecord> getAnomaliesByShift(Long shiftId) {
        return anomalyRepository.findByShiftId(shiftId);
    }

    public List<AnomalyRecord> getUnresolvedAnomalies() {
        return anomalyRepository.findByResolved(false);
    }

    public List<AnomalyRecord> getUnresolvedAnomaliesByShift(Long shiftId) {
        return anomalyRepository.findUnresolvedByShiftId(shiftId);
    }

    public List<AnomalyRecord> getAllAnomalies() {
        return anomalyRepository.findAll();
    }
}
