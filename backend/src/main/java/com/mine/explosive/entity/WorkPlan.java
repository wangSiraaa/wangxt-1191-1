package com.mine.explosive.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;


import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "work_plan")
public class WorkPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String planNo;

    @Column(nullable = false)
    private String workFace;

    @Column(nullable = false)
    private Integer designedHoles;

    @Column(nullable = false)
    private Integer estimatedDetonators;

    @Column(nullable = false)
    private Integer estimatedExplosives;

    private LocalDate workDate;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blaster_id")
    private User blaster;

    private String description;

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

    public String getPlanNo() {
        return planNo;
    }

    public void setPlanNo(String planNo) {
        this.planNo = planNo;
    }

    public String getWorkFace() {
        return workFace;
    }

    public void setWorkFace(String workFace) {
        this.workFace = workFace;
    }

    public Integer getDesignedHoles() {
        return designedHoles;
    }

    public void setDesignedHoles(Integer designedHoles) {
        this.designedHoles = designedHoles;
    }

    public Integer getEstimatedDetonators() {
        return estimatedDetonators;
    }

    public void setEstimatedDetonators(Integer estimatedDetonators) {
        this.estimatedDetonators = estimatedDetonators;
    }

    public Integer getEstimatedExplosives() {
        return estimatedExplosives;
    }

    public void setEstimatedExplosives(Integer estimatedExplosives) {
        this.estimatedExplosives = estimatedExplosives;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public User getBlaster() {
        return blaster;
    }

    public void setBlaster(User blaster) {
        this.blaster = blaster;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
