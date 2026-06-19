package com.mine.explosive.repository;

import com.mine.explosive.entity.WorkPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkPlanRepository extends JpaRepository<WorkPlan, Long> {
    Optional<WorkPlan> findByPlanNo(String planNo);
    List<WorkPlan> findByWorkDate(LocalDate workDate);
    List<WorkPlan> findByBlasterId(Long blasterId);
    List<WorkPlan> findByWorkFace(String workFace);
}
