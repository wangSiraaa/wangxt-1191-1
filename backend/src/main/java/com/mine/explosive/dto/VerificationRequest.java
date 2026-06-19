package com.mine.explosive.dto;

import jakarta.validation.constraints.NotNull;

public class VerificationRequest {
    public VerificationRequest() {
    }
    public VerificationRequest(Long shiftId, Long applicationId, Integer expectedDetonators, Integer expectedExplosives, Integer usedDetonators, Integer returnedDetonators, Integer usedExplosives, Integer returnedExplosives, Boolean allReturned, String verificationRemark) {
        this.shiftId = shiftId;
        this.applicationId = applicationId;
        this.expectedDetonators = expectedDetonators;
        this.expectedExplosives = expectedExplosives;
        this.usedDetonators = usedDetonators;
        this.returnedDetonators = returnedDetonators;
        this.usedExplosives = usedExplosives;
        this.returnedExplosives = returnedExplosives;
        this.allReturned = allReturned;
        this.verificationRemark = verificationRemark;
    }
    @NotNull(message = "当班作业ID不能为空")
    private Long shiftId;

    @NotNull(message = "申请单ID不能为空")
    private Long applicationId;

    @NotNull(message = "应发雷管数量不能为空")
    private Integer expectedDetonators;

    @NotNull(message = "应发炸药数量不能为空")
    private Integer expectedExplosives;

    @NotNull(message = "已使用雷管数量不能为空")
    private Integer usedDetonators;

    @NotNull(message = "退回雷管数量不能为空")
    private Integer returnedDetonators;

    @NotNull(message = "已使用炸药数量不能为空")
    private Integer usedExplosives;

    @NotNull(message = "退回炸药数量不能为空")
    private Integer returnedExplosives;

    private Boolean allReturned;

    private String verificationRemark;

    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public Integer getExpectedDetonators() { return expectedDetonators; }
    public void setExpectedDetonators(Integer expectedDetonators) { this.expectedDetonators = expectedDetonators; }
    public Integer getExpectedExplosives() { return expectedExplosives; }
    public void setExpectedExplosives(Integer expectedExplosives) { this.expectedExplosives = expectedExplosives; }
    public Integer getUsedDetonators() { return usedDetonators; }
    public void setUsedDetonators(Integer usedDetonators) { this.usedDetonators = usedDetonators; }
    public Integer getReturnedDetonators() { return returnedDetonators; }
    public void setReturnedDetonators(Integer returnedDetonators) { this.returnedDetonators = returnedDetonators; }
    public Integer getUsedExplosives() { return usedExplosives; }
    public void setUsedExplosives(Integer usedExplosives) { this.usedExplosives = usedExplosives; }
    public Integer getReturnedExplosives() { return returnedExplosives; }
    public void setReturnedExplosives(Integer returnedExplosives) { this.returnedExplosives = returnedExplosives; }
    public Boolean getAllReturned() { return allReturned; }
    public void setAllReturned(Boolean allReturned) { this.allReturned = allReturned; }
    public String getVerificationRemark() { return verificationRemark; }
    public void setVerificationRemark(String verificationRemark) { this.verificationRemark = verificationRemark; }
}
