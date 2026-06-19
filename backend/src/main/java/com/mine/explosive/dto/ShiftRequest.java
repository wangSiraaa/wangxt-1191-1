package com.mine.explosive.dto;

import jakarta.validation.constraints.NotBlank;



public class ShiftRequest {
    public ShiftRequest() {
    }
    public ShiftRequest(String workFace, Long workPlanId, String remarks) {
        this.workFace = workFace;
        this.workPlanId = workPlanId;
        this.remarks = remarks;
    }
    @NotBlank(message = "作业面不能为空")
    private String workFace;

    private Long workPlanId;

    private String remarks;

    public String getWorkFace() {
        return workFace;
    }

    public void setWorkFace(String workFace) {
        this.workFace = workFace;
    }

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
