package com.mine.explosive.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.mine.explosive.dto.PickupApplicationRequest;
import com.mine.explosive.dto.ReviewRequest;
import com.mine.explosive.entity.*;
import com.mine.explosive.enums.AnomalyType;
import com.mine.explosive.enums.ApplicationStatus;
import com.mine.explosive.enums.Role;
import com.mine.explosive.enums.ShiftStatus;
import com.mine.explosive.exception.BusinessException;
import com.mine.explosive.repository.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service

public class PickupApplicationService {

    @Autowired
    public PickupApplicationService(PickupApplicationRepository applicationRepository, ShiftRepository shiftRepository, BlasterRepository blasterRepository, WorkPlanRepository workPlanRepository, ExplosiveRepository explosiveRepository, AnomalyRecordRepository anomalyRecordRepository, UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.shiftRepository = shiftRepository;
        this.blasterRepository = blasterRepository;
        this.workPlanRepository = workPlanRepository;
        this.explosiveRepository = explosiveRepository;
        this.anomalyRecordRepository = anomalyRecordRepository;
        this.userRepository = userRepository;
    }

    private final PickupApplicationRepository applicationRepository;
    private final ShiftRepository shiftRepository;
    private final BlasterRepository blasterRepository;
    private final WorkPlanRepository workPlanRepository;
    private final ExplosiveRepository explosiveRepository;
    private final AnomalyRecordRepository anomalyRecordRepository;
    private final UserRepository userRepository;

    @Transactional
    public PickupApplication createApplication(PickupApplicationRequest request, User blaster) {
        if (blaster.getRole() != Role.BLASTER) {
            throw new BusinessException("只有爆破员可以创建领用申请");
        }

        Shift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new BusinessException("当班作业不存在"));

        if (!shift.getBlaster().getId().equals(blaster.getId())) {
            throw new BusinessException("只能为自己的当班作业创建申请");
        }

        if (shift.getStatus() == ShiftStatus.CLOSED) {
            throw new BusinessException("当班作业已关闭，无法创建申请");
        }

        Blaster blasterInfo = blasterRepository.findByUserId(blaster.getId())
                .orElseThrow(() -> new BusinessException("未找到爆破员信息"));

        if (blasterInfo.isLicenseExpired()) {
            recordAnomaly(shift, null, AnomalyType.EXPIRED_LICENSE,
                    "爆破员作业证已过期，有效期至: " + blasterInfo.getLicenseExpiryDate(), blaster);
            throw new BusinessException("作业证已过期，有效期至: " + blasterInfo.getLicenseExpiryDate() + "，无法领用器材");
        }

        if (blasterInfo.getLicenseExpiryDate().isBefore(LocalDate.now().plusDays(7))) {
            recordAnomaly(shift, null, AnomalyType.EXPIRED_LICENSE,
                    "爆破员作业证即将过期，有效期至: " + blasterInfo.getLicenseExpiryDate(), blaster);
        }

        ApplicationStatus status = ApplicationStatus.PENDING;
        String reviewRemark = null;

        if (shift.getWorkPlan() != null) {
            WorkPlan workPlan = shift.getWorkPlan();
            int designedDetonators = workPlan.getEstimatedDetonators();
            int designedExplosives = workPlan.getEstimatedExplosives();
            int designedHoles = workPlan.getDesignedHoles();

            boolean detonatorMismatch = Math.abs(request.getDetonatorQuantity() - designedDetonators) > designedHoles * 0.1;
            boolean explosiveMismatch = Math.abs(request.getExplosiveQuantity() - designedExplosives) > designedHoles * 2;

            if (detonatorMismatch || explosiveMismatch) {
                status = ApplicationStatus.NEED_REVIEW;
                reviewRemark = String.format("领用数量与设计孔数不匹配。设计孔数: %d, 设计雷管: %d, 申请雷管: %d, 设计炸药: %dkg, 申请炸药: %dkg",
                        designedHoles, designedDetonators, request.getDetonatorQuantity(),
                        designedExplosives, request.getExplosiveQuantity());

                recordAnomaly(shift, null, AnomalyType.QUANTITY_MISMATCH, reviewRemark, blaster);
            }
        }

        Integer availableDetonators = explosiveRepository.sumAvailableQuantityByType(
                com.mine.explosive.enums.ExplosiveType.DETONATOR);
        Integer availableExplosives = explosiveRepository.sumAvailableQuantityByType(
                com.mine.explosive.enums.ExplosiveType.EXPLOSIVE);

        if (availableDetonators != null && availableDetonators < request.getDetonatorQuantity()) {
            throw new BusinessException("雷管库存不足，现有库存: " + availableDetonators);
        }
        if (availableExplosives != null && availableExplosives < request.getExplosiveQuantity()) {
            throw new BusinessException("炸药库存不足，现有库存: " + availableExplosives + "kg");
        }

        PickupApplication application = new PickupApplication();
        application.setApplicationNo("APP" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        application.setShift(shift);
        application.setBlaster(blaster);
        application.setDetonatorQuantity(request.getDetonatorQuantity());
        application.setExplosiveQuantity(request.getExplosiveQuantity());
        application.setStatus(status);
        application.setReviewRemark(reviewRemark);

        if (status == ApplicationStatus.NEED_REVIEW) {
            shift.setStatus(ShiftStatus.IN_PROGRESS);
        } else {
            application.setStatus(ApplicationStatus.APPROVED);
            shift.setStatus(ShiftStatus.IN_PROGRESS);
        }
        shiftRepository.save(shift);

        return applicationRepository.save(application);
    }

    @Transactional
    public PickupApplication reviewApplication(ReviewRequest request, User reviewer) {
        if (reviewer.getRole() != Role.SAFETY_OFFICER && reviewer.getRole() != Role.STOREKEEPER) {
            throw new BusinessException("只有安全负责人或库管可以审核申请");
        }

        PickupApplication application = applicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new BusinessException("申请单不存在"));

        if (application.getStatus() != ApplicationStatus.NEED_REVIEW) {
            throw new BusinessException("该申请不需要复核");
        }

        application.setStatus(request.getApproved() ? ApplicationStatus.APPROVED : ApplicationStatus.REJECTED);
        application.setReviewer(reviewer);
        application.setReviewRemark(request.getRemark());
        application.setReviewedAt(LocalDateTime.now());

        return applicationRepository.save(application);
    }

    public PickupApplication getApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("申请单不存在"));
    }

    public List<PickupApplication> getApplicationsByShift(Long shiftId) {
        return applicationRepository.findByShiftId(shiftId);
    }

    public List<PickupApplication> getApplicationsByBlaster(Long blasterId) {
        return applicationRepository.findByBlasterId(blasterId);
    }

    public List<PickupApplication> getApplicationsNeedReview() {
        return applicationRepository.findApplicationsNeedReview();
    }

    public List<PickupApplication> getAllApplications() {
        return applicationRepository.findAll();
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
