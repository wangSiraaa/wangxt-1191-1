package com.mine.explosive.service;

import org.springframework.beans.factory.annotation.Autowired;
import com.mine.explosive.dto.HoleChangeRequest;
import com.mine.explosive.dto.ReviewRequest;
import com.mine.explosive.entity.*;
import com.mine.explosive.enums.ApplicationStatus;
import com.mine.explosive.enums.Role;
import com.mine.explosive.exception.BusinessException;
import com.mine.explosive.repository.*;
import com.mine.explosive.util.HibernateUtil;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class HoleChangeService {

    @Autowired
    public HoleChangeService(HoleChangeRecordRepository holeChangeRepository, ShiftRepository shiftRepository,
                             PickupApplicationRepository applicationRepository, UserRepository userRepository) {
        this.holeChangeRepository = holeChangeRepository;
        this.shiftRepository = shiftRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    private final HoleChangeRecordRepository holeChangeRepository;
    private final ShiftRepository shiftRepository;
    private final PickupApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    @Transactional
    public HoleChangeRecord createHoleChange(HoleChangeRequest request, User requester) {
        if (requester.getRole() != Role.BLASTER) {
            throw new BusinessException("只有爆破员可以申请孔数变更");
        }

        Shift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new BusinessException("当班作业不存在"));

        if (!shift.getBlaster().getId().equals(requester.getId())) {
            throw new BusinessException("只能为自己的当班作业申请孔数变更");
        }

        if (shift.getStatus() == com.mine.explosive.enums.ShiftStatus.CLOSED) {
            throw new BusinessException("当班作业已关闭，无法申请孔数变更");
        }

        PickupApplication application = null;
        if (request.getApplicationId() != null) {
            application = applicationRepository.findById(request.getApplicationId())
                    .orElseThrow(() -> new BusinessException("领用申请不存在"));
        }

        int holeDiff = request.getNewHoles() - request.getOriginalHoles();
        int detonatorDiff = request.getNewDetonators() - request.getOriginalDetonators();
        int explosiveDiff = request.getNewExplosives() - request.getOriginalExplosives();

        boolean needsReview = (holeDiff != 0) || (detonatorDiff != 0) || (explosiveDiff != 0);

        HoleChangeRecord record = new HoleChangeRecord();
        record.setChangeNo("HC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        record.setShift(shift);
        record.setApplication(application);
        record.setOriginalHoles(request.getOriginalHoles());
        record.setNewHoles(request.getNewHoles());
        record.setHoleDifference(holeDiff);
        record.setOriginalDetonators(request.getOriginalDetonators());
        record.setNewDetonators(request.getNewDetonators());
        record.setOriginalExplosives(request.getOriginalExplosives());
        record.setNewExplosives(request.getNewExplosives());
        record.setChangeReason(request.getChangeReason());
        record.setRequestedBy(requester);
        record.setStatus(needsReview ? ApplicationStatus.NEED_REVIEW : ApplicationStatus.APPROVED);

        if (!needsReview) {
            record.setReviewedBy(requester);
            record.setReviewedAt(LocalDateTime.now());
            record.setReviewRemark("数量无变化，自动通过");
            updateShiftHoles(shift, request.getNewHoles());
        }

        return holeChangeRepository.save(record);
    }

    @Transactional
    public HoleChangeRecord reviewHoleChange(ReviewRequest request, User reviewer) {
        if (reviewer.getRole() != Role.SAFETY_OFFICER) {
            throw new BusinessException("只有安全负责人可以复核孔数变更");
        }

        HoleChangeRecord record = holeChangeRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new BusinessException("孔数变更记录不存在"));

        if (record.getStatus() != ApplicationStatus.NEED_REVIEW) {
            throw new BusinessException("该孔数变更不需要复核");
        }

        record.setStatus(request.getApproved() ? ApplicationStatus.APPROVED : ApplicationStatus.REJECTED);
        record.setReviewedBy(reviewer);
        record.setReviewedAt(LocalDateTime.now());
        record.setReviewRemark(request.getRemark());

        if (request.getApproved()) {
            updateShiftHoles(record.getShift(), record.getNewHoles());
        }

        HoleChangeRecord saved = holeChangeRepository.save(record);
        HibernateUtil.initHoleChangeRecord(saved);
        return saved;
    }

    private void updateShiftHoles(Shift shift, Integer newHoles) {
        shift.setActualHoles(newHoles);
        shiftRepository.save(shift);
    }

    public HoleChangeRecord getHoleChange(Long id) {
        HoleChangeRecord record = holeChangeRepository.findById(id)
                .orElseThrow(() -> new BusinessException("孔数变更记录不存在"));
        HibernateUtil.initHoleChangeRecord(record);
        return record;
    }

    public List<HoleChangeRecord> getHoleChangesByShift(Long shiftId) {
        List<HoleChangeRecord> records = holeChangeRepository.findByShiftId(shiftId);
        records.forEach(HibernateUtil::initHoleChangeRecord);
        return records;
    }

    public List<HoleChangeRecord> getHoleChangesByApplication(Long applicationId) {
        List<HoleChangeRecord> records = holeChangeRepository.findByApplicationId(applicationId);
        records.forEach(HibernateUtil::initHoleChangeRecord);
        return records;
    }

    public List<HoleChangeRecord> getHoleChangesNeedReview() {
        List<HoleChangeRecord> records = holeChangeRepository.findAllNeedReview();
        records.forEach(HibernateUtil::initHoleChangeRecord);
        return records;
    }

    public List<HoleChangeRecord> getAllHoleChanges() {
        List<HoleChangeRecord> records = holeChangeRepository.findAll();
        records.forEach(HibernateUtil::initHoleChangeRecord);
        return records;
    }
}
