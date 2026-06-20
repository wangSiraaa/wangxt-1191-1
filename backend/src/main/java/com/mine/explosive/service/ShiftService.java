package com.mine.explosive.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.mine.explosive.dto.ShiftRequest;
import com.mine.explosive.entity.Shift;
import com.mine.explosive.entity.User;
import com.mine.explosive.entity.WorkPlan;
import com.mine.explosive.enums.ApplicationStatus;
import com.mine.explosive.enums.ShiftStatus;
import com.mine.explosive.exception.BusinessException;
import com.mine.explosive.repository.PickupApplicationRepository;
import com.mine.explosive.repository.ShiftRepository;
import com.mine.explosive.repository.VerificationRecordRepository;
import com.mine.explosive.repository.WorkPlanRepository;
import com.mine.explosive.util.HibernateUtil;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service

public class ShiftService {

    @Autowired
    public ShiftService(ShiftRepository shiftRepository, WorkPlanRepository workPlanRepository, PickupApplicationRepository applicationRepository, VerificationRecordRepository verificationRecordRepository) {
        this.shiftRepository = shiftRepository;
        this.workPlanRepository = workPlanRepository;
        this.applicationRepository = applicationRepository;
        this.verificationRecordRepository = verificationRecordRepository;
    }

    private final ShiftRepository shiftRepository;
    private final WorkPlanRepository workPlanRepository;
    private final PickupApplicationRepository applicationRepository;
    private final VerificationRecordRepository verificationRecordRepository;

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

        if (!applicationRepository.findByShiftIdAndStatusIn(shiftId, incompleteStatuses).isEmpty()) {
            throw new BusinessException("存在未完成回库的申请，无法关闭当班作业");
        }

        if (!verificationRecordRepository.existsByApplicationId(
                applicationRepository.findByShiftId(shiftId).stream()
                        .map(app -> app.getId())
                        .findFirst()
                        .orElse(0L)
        ) && !applicationRepository.findByShiftId(shiftId).isEmpty()) {
            throw new BusinessException("存在未经过安全负责人核对的申请，无法关闭当班作业");
        }

        shift.setStatus(ShiftStatus.CLOSED);
        shift.setEndTime(LocalDateTime.now());
        return shiftRepository.save(shift);
    }
}
