package com.mine.explosive.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;



public class OutboundRequest {
    public OutboundRequest() {
    }
    public OutboundRequest(Long applicationId, String explosiveSerialNo, Integer quantity) {
        this.applicationId = applicationId;
        this.explosiveSerialNo = explosiveSerialNo;
        this.quantity = quantity;
    }
    @NotNull(message = "申请单ID不能为空")
    private Long applicationId;

    @NotBlank(message = "器材编号不能为空")
    private String explosiveSerialNo;

    @NotNull(message = "出库数量不能为空")
    private Integer quantity;

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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
