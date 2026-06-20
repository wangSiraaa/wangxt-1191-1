package com.mine.explosive.service;

import com.mine.explosive.entity.WorkPlan;
import com.mine.explosive.entity.User;
import com.mine.explosive.exception.BusinessException;
import com.mine.explosive.repository.WorkPlanRepository;
import com.mine.explosive.util.HibernateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkPlanService {

    private final WorkPlanRepository workPlanRepository;

    @Autowired
    public WorkPlanService(WorkPlanRepository workPlanRepository) {
        this.workPlanRepository = workPlanRepository;
    }

    public List<WorkPlan> getAllWorkPlans() {
        List<WorkPlan> plans = workPlanRepository.findAll();
        plans.forEach(HibernateUtil::initWorkPlan);
        return plans;
    }

    public List<WorkPlan> getMyWorkPlans(Long blasterId) {
        List<WorkPlan> plans = workPlanRepository.findByBlasterId(blasterId);
        plans.forEach(HibernateUtil::initWorkPlan);
        return plans;
    }

    public WorkPlan getWorkPlan(Long id) {
        WorkPlan plan = workPlanRepository.findById(id)
                .orElseThrow(() -> new BusinessException("作业计划不存在"));
        HibernateUtil.initWorkPlan(plan);
        return plan;
    }
}
