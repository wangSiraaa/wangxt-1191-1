package com.mine.explosive.repository;

import com.mine.explosive.entity.PickupApplication;
import com.mine.explosive.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PickupApplicationRepository extends JpaRepository<PickupApplication, Long> {
    Optional<PickupApplication> findByApplicationNo(String applicationNo);
    List<PickupApplication> findByBlasterId(Long blasterId);
    List<PickupApplication> findByStatus(ApplicationStatus status);
    List<PickupApplication> findByShiftId(Long shiftId);

    @Query("SELECT p FROM PickupApplication p WHERE p.shift.id = :shiftId AND p.status IN :statuses")
    List<PickupApplication> findByShiftIdAndStatusIn(Long shiftId, List<ApplicationStatus> statuses);

    @Query("SELECT COUNT(p) > 0 FROM PickupApplication p WHERE p.shift.id = :shiftId AND p.status != 'CLOSED'")
    boolean existsActiveApplicationByShiftId(Long shiftId);

    @Query("SELECT p FROM PickupApplication p WHERE p.status = 'NEED_REVIEW'")
    List<PickupApplication> findApplicationsNeedReview();
}
