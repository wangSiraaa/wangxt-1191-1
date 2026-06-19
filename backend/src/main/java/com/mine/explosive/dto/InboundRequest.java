package com.mine.explosive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;



public class InboundRequest {
    public InboundRequest() {
    }
    public InboundRequest(Long applicationId, String explosiveSerialNo, Integer usedQuantity, Integer returnedQuantity, String remarks) {
        this.applicationId = applicationId;
        this.explosiveSerialNo = explosiveSerialNo;
        this.usedQuantity = usedQuantity;
        this.returnedQuantity = returnedQuantity;
        this.remarks = remarks;
    }
    @NotNull(message = "申请单ID不能为空")
    private Long applicationId;

    @NotBlank(message = "器材编号不能为空")
    private String explosiveSerialNo;

    @NotNull(message = "使用数量不能为空")
    private Integer usedQuantity;

    @NotNull(message = "退回数量不能为空")
    private Integer returnedQuantity;

    private String remarks;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getExplosiveSerialNo() {
        return explosiveSerialNo;
    }

    public void setExplosiveSerialNo(String explosiveSerialNo) {
        this.explosiveSerialNo = explosiveSerialNo;
    }

    public Integer getUsedQuantity() {
        return usedQuantity;
    }

    public void setUsedQuantity(Integer usedQuantity) {
        this.usedQuantity = usedQuantity;
    }

    public Integer getReturnedQuantity() {
        return returnedQuantity;
    }

    public void setReturnedQuantity(Integer returnedQuantity) {
        this.returnedQuantity = returnedQuantity;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
