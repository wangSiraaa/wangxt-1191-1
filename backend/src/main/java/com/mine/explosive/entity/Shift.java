package com.mine.explosive.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mine.explosive.enums.ShiftStatus;
import jakarta.persistence.*;


import java.time.LocalDateTime;


@Entity
@Table(name = "shift")
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String shiftNo;

    @Column(nullable = false)
    private String workFace;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_plan_id")
    private WorkPlan workPlan;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blaster_id", nullable = false)
    private User blaster;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftStatus status;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String remarks;

    @Column(nullable = false)
    private Integer actualHoles = 0;

    @Column(nullable = false)
    private Boolean remainingCleared = false;

    @Column(nullable = false)
    private Boolean misfireHandled = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShiftNo() {
        return shiftNo;
    }

    public void setShiftNo(String shiftNo) {
        this.shiftNo = shiftNo;
    }

    public String getWorkFace() {
        return workFace;
    }

    public void setWorkFace(String workFace) {
        this.workFace = workFace;
    }

    public WorkPlan getWorkPlan() {
        return workPlan;
    }

    public void setWorkPlan(WorkPlan workPlan) {
        this.workPlan = workPlan;
    }

    public User getBlaster() {
        return blaster;
    }

    public void setBlaster(User blaster) {
        this.blaster = blaster;
    }

    public ShiftStatus getStatus() {
        return status;
    }

    public void setStatus(ShiftStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getActualHoles() {
        return actualHoles;
    }

    public void setActualHoles(Integer actualHoles) {
        this.actualHoles = actualHoles;
    }

    public Boolean getRemainingCleared() {
        return remainingCleared;
    }

    public void setRemainingCleared(Boolean remainingCleared) {
        this.remainingCleared = remainingCleared;
    }

    public Boolean getMisfireHandled() {
        return misfireHandled;
    }

    public void setMisfireHandled(Boolean misfireHandled) {
        this.misfireHandled = misfireHandled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
