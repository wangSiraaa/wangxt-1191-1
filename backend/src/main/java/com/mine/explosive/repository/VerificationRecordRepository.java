package com.mine.explosive.repository;

import com.mine.explosive.entity.VerificationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationRecordRepository extends JpaRepository<VerificationRecord, Long> {
    Optional<VerificationRecord> findByVerificationNo(String verificationNo);
    List<VerificationRecord> findByShiftId(Long shiftId);
    List<VerificationRecord> findByApplicationId(Long applicationId);
    List<VerificationRecord> findBySafetyOfficerId(Long safetyOfficerId);

    @Query("SELECT v FROM VerificationRecord v WHERE v.verificationTime BETWEEN :start AND :end")
    List<VerificationRecord> findByVerificationTimeBetween(LocalDateTime start, LocalDateTime end);

    boolean existsByApplicationId(Long applicationId);
}
