package com.mine.explosive.dto;

import com.mine.explosive.enums.AnomalyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AnomalyRequest {
    public AnomalyRequest() {
    }
    public AnomalyRequest(Long shiftId, Long applicationId, AnomalyType type, String description, String explosiveSerialNo, Integer anomalyQuantity) {
        this.shiftId = shiftId;
        this.applicationId = applicationId;
        this.type = type;
        this.description = description;
        this.explosiveSerialNo = explosiveSerialNo;
        this.anomalyQuantity = anomalyQuantity;
    }
    private Long shiftId;
    private Long applicationId;

    @NotNull(message = "异常类型不能为空")
    private AnomalyType type;

    @NotBlank(message = "异常描述不能为空")
    private String description;

    private String explosiveSerialNo;
    private Integer anomalyQuantity;

    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public AnomalyType getType() { return type; }
    public void setType(AnomalyType type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getExplosiveSerialNo() { return explosiveSerialNo; }
    public void setExplosiveSerialNo(String explosiveSerialNo) { this.explosiveSerialNo = explosiveSerialNo; }
    public Integer getAnomalyQuantity() { return anomalyQuantity; }
    public void setAnomalyQuantity(Integer anomalyQuantity) { this.anomalyQuantity = anomalyQuantity; }
}
