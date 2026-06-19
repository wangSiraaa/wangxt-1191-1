package com.mine.explosive.repository;

import com.mine.explosive.entity.AnomalyRecord;
import com.mine.explosive.enums.AnomalyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnomalyRecordRepository extends JpaRepository<AnomalyRecord, Long> {
    Optional<AnomalyRecord> findByRecordNo(String recordNo);
    List<AnomalyRecord> findByShiftId(Long shiftId);
    List<AnomalyRecord> findByApplicationId(Long applicationId);
    List<AnomalyRecord> findByReportedById(Long userId);
    List<AnomalyRecord> findByResolved(boolean resolved);
    List<AnomalyRecord> findByType(AnomalyType type);

    @Query("SELECT a FROM AnomalyRecord a WHERE a.reportedAt BETWEEN :start AND :end")
    List<AnomalyRecord> findByReportedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT a FROM AnomalyRecord a WHERE a.shift.id = :shiftId AND a.resolved = false")
    List<AnomalyRecord> findUnresolvedByShiftId(Long shiftId);
}
