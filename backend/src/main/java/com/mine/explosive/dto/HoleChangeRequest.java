package com.mine.explosive.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class HoleChangeRequest {

    public HoleChangeRequest() {
    }

    public HoleChangeRequest(Long shiftId, Long applicationId, Integer originalHoles, Integer newHoles,
                             Integer originalDetonators, Integer newDetonators,
                             Integer originalExplosives, Integer newExplosives, String changeReason) {
        this.shiftId = shiftId;
        this.applicationId = applicationId;
        this.originalHoles = originalHoles;
        this.newHoles = newHoles;
        this.originalDetonators = originalDetonators;
        this.newDetonators = newDetonators;
        this.originalExplosives = originalExplosives;
        this.newExplosives = newExplosives;
        this.changeReason = changeReason;
    }

    @NotNull(message = "当班作业ID不能为空")
    private Long shiftId;

    private Long applicationId;

    @NotNull(message = "原始孔数不能为空")
    @Min(value = 0, message = "原始孔数不能为负数")
    private Integer originalHoles;

    @NotNull(message = "新孔数不能为空")
    @Min(value = 0, message = "新孔数不能为负数")
    private Integer newHoles;

    @NotNull(message = "原始雷管数量不能为空")
    @Min(value = 0, message = "原始雷管数量不能为负数")
    private Integer originalDetonators;

    @NotNull(message = "新雷管数量不能为空")
    @Min(value = 0, message = "新雷管数量不能为负数")
    private Integer newDetonators;

    @NotNull(message = "原始炸药数量不能为空")
    @Min(value = 0, message = "原始炸药数量不能为负数")
    private Integer originalExplosives;

    @NotNull(message = "新炸药数量不能为空")
    @Min(value = 0, message = "新炸药数量不能为负数")
    private Integer newExplosives;

    @NotBlank(message = "变更原因不能为空")
    private String changeReason;

    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getOriginalHoles() {
        return originalHoles;
    }

    public void setOriginalHoles(Integer originalHoles) {
        this.originalHoles = originalHoles;
    }

    public Integer getNewHoles() {
        return newHoles;
    }

    public void setNewHoles(Integer newHoles) {
        this.newHoles = newHoles;
    }

    public Integer getOriginalDetonators() {
        return originalDetonators;
    }

    public void setOriginalDetonators(Integer originalDetonators) {
        this.originalDetonators = originalDetonators;
    }

    public Integer getNewDetonators() {
        return newDetonators;
    }

    public void setNewDetonators(Integer newDetonators) {
        this.newDetonators = newDetonators;
    }

    public Integer getOriginalExplosives() {
        return originalExplosives;
    }

    public void setOriginalExplosives(Integer originalExplosives) {
        this.originalExplosives = originalExplosives;
    }

    public Integer getNewExplosives() {
        return newExplosives;
    }

    public void setNewExplosives(Integer newExplosives) {
        this.newExplosives = newExplosives;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }
}
