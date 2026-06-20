package com.mine.explosive.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hole_change_record")
public class HoleChangeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String changeNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private PickupApplication application;

    @Column(nullable = false)
    private Integer originalHoles;

    @Column(nullable = false)
    private Integer newHoles;

    @Column(nullable = false)
    private Integer holeDifference;

    @Column(nullable = false)
    private Integer originalDetonators;

    @Column(nullable = false)
    private Integer newDetonators;

    @Column(nullable = false)
    private Integer originalExplosives;

    @Column(nullable = false)
    private Integer newExplosives;

    @Column(nullable = false)
    private String changeReason;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private com.mine.explosive.enums.ApplicationStatus status;

    private String reviewRemark;

    private LocalDateTime reviewedAt;

    private LocalDateTime requestedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        requestedAt = LocalDateTime.now();
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

    public String getChangeNo() {
        return changeNo;
    }

    public void setChangeNo(String changeNo) {
        this.changeNo = changeNo;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public PickupApplication getApplication() {
        return application;
    }

    public void setApplication(PickupApplication application) {
        this.application = application;
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

    public Integer getHoleDifference() {
        return holeDifference;
    }

    public void setHoleDifference(Integer holeDifference) {
        this.holeDifference = holeDifference;
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

    public User getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(User requestedBy) {
        this.requestedBy = requestedBy;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public com.mine.explosive.enums.ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(com.mine.explosive.enums.ApplicationStatus status) {
        this.status = status;
    }

    public String getReviewRemark() {
        return reviewRemark;
    }

    public void setReviewRemark(String reviewRemark) {
        this.reviewRemark = reviewRemark;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
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
