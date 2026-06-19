package com.mine.explosive.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;



public class PickupApplicationRequest {
    public PickupApplicationRequest() {
    }
    public PickupApplicationRequest(Long shiftId, Integer detonatorQuantity, Integer explosiveQuantity, String remarks) {
        this.shiftId = shiftId;
        this.detonatorQuantity = detonatorQuantity;
        this.explosiveQuantity = explosiveQuantity;
        this.remarks = remarks;
    }
    @NotNull(message = "当班作业ID不能为空")
    private Long shiftId;

    @NotNull(message = "雷管数量不能为空")
    @Min(value = 0, message = "雷管数量不能为负数")
    private Integer detonatorQuantity;

    @NotNull(message = "炸药数量不能为空")
    @Min(value = 0, message = "炸药数量不能为负数")
    private Integer explosiveQuantity;

    private String remarks;

    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }

    public Integer getDetonatorQuantity() {
        return detonatorQuantity;
    }

    public void setDetonatorQuantity(Integer detonatorQuantity) {
        this.detonatorQuantity = detonatorQuantity;
    }

    public Integer getExplosiveQuantity() {
        return explosiveQuantity;
    }

    public void setExplosiveQuantity(Integer explosiveQuantity) {
        this.explosiveQuantity = explosiveQuantity;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
