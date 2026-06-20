package com.mine.explosive.dto;

import jakarta.validation.constraints.NotNull;



public class ShiftRequest {
    public ShiftRequest() {
    }
    public ShiftRequest(Long workPlanId, String remarks) {
        this.workPlanId = workPlanId;
        this.remarks = remarks;
    }

    @NotNull(message = "请选择作业计划")
    private Long workPlanId;

    private String remarks;

    public Long getWorkPlanId() {
        return workPlanId;
    }

    public void setWorkPlanId(Long workPlanId) {
        this.workPlanId = workPlanId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
