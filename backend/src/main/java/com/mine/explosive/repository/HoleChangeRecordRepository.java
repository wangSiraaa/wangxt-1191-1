package com.mine.explosive.repository;

import com.mine.explosive.entity.HoleChangeRecord;
import com.mine.explosive.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoleChangeRecordRepository extends JpaRepository<HoleChangeRecord, Long> {
    Optional<HoleChangeRecord> findByChangeNo(String changeNo);
    List<HoleChangeRecord> findByShiftId(Long shiftId);
    List<HoleChangeRecord> findByApplicationId(Long applicationId);
    List<HoleChangeRecord> findByStatus(ApplicationStatus status);

    @Query("SELECT h FROM HoleChangeRecord h WHERE h.shift.id = :shiftId AND h.status = 'NEED_REVIEW'")
    List<HoleChangeRecord> findPendingByShiftId(Long shiftId);

    @Query("SELECT h FROM HoleChangeRecord h WHERE h.status = 'NEED_REVIEW'")
    List<HoleChangeRecord> findAllNeedReview();

    @Query("SELECT COUNT(h) > 0 FROM HoleChangeRecord h WHERE h.shift.id = :shiftId AND h.status = 'NEED_REVIEW'")
    boolean existsPendingByShiftId(Long shiftId);
}
