package com.mine.explosive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;



public class ReviewRequest {
    public ReviewRequest() {
    }
    public ReviewRequest(Long applicationId, Boolean approved, String remark) {
        this.applicationId = applicationId;
        this.approved = approved;
        this.remark = remark;
    }
    @NotNull(message = "申请单ID不能为空")
    private Long applicationId;

    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    @NotBlank(message = "审核意见不能为空")
    private String remark;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
