package com.mine.explosive.repository;

import com.mine.explosive.entity.Shift;
import com.mine.explosive.enums.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {
    Optional<Shift> findByShiftNo(String shiftNo);
    List<Shift> findByBlasterId(Long blasterId);
    List<Shift> findByStatus(ShiftStatus status);
    List<Shift> findByWorkFace(String workFace);

    @Query("SELECT s FROM Shift s WHERE s.blaster.id = :blasterId AND s.status != 'CLOSED'")
    List<Shift> findActiveShiftsByBlasterId(Long blasterId);

    @Query("SELECT s FROM Shift s WHERE s.startTime BETWEEN :start AND :end")
    List<Shift> findByStartTimeBetween(LocalDateTime start, LocalDateTime end);

    boolean existsByBlasterIdAndStatusNot(Long blasterId, ShiftStatus status);
}
