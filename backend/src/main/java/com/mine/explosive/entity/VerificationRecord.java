package com.mine.explosive.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;


import java.time.LocalDateTime;


@Entity
@Table(name = "verification_record")
public class VerificationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String verificationNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private PickupApplication application;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "safety_officer_id", nullable = false)
    private User safetyOfficer;

    @Column(nullable = false)
    private Integer expectedDetonators;

    @Column(nullable = false)
    private Integer usedDetonators;

    @Column(nullable = false)
    private Integer returnedDetonators;

    @Column(nullable = false)
    private Integer expectedExplosives;

    @Column(nullable = false)
    private Integer usedExplosives;

    @Column(nullable = false)
    private Integer returnedExplosives;

    @Column(nullable = false)
    private boolean allReturned;

    private String verificationRemark;

    @Column(nullable = false)
    private LocalDateTime verificationTime;

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

    public String getVerificationNo() {
        return verificationNo;
    }

    public void setVerificationNo(String verificationNo) {
        this.verificationNo = verificationNo;
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

    public User getSafetyOfficer() {
        return safetyOfficer;
    }

    public void setSafetyOfficer(User safetyOfficer) {
        this.safetyOfficer = safetyOfficer;
    }

    public Integer getExpectedDetonators() {
        return expectedDetonators;
    }

    public void setExpectedDetonators(Integer expectedDetonators) {
        this.expectedDetonators = expectedDetonators;
    }

    public Integer getUsedDetonators() {
        return usedDetonators;
    }

    public void setUsedDetonators(Integer usedDetonators) {
        this.usedDetonators = usedDetonators;
    }

    public Integer getReturnedDetonators() {
        return returnedDetonators;
    }

    public void setReturnedDetonators(Integer returnedDetonators) {
        this.returnedDetonators = returnedDetonators;
    }

    public Integer getExpectedExplosives() {
        return expectedExplosives;
    }

    public void setExpectedExplosives(Integer expectedExplosives) {
        this.expectedExplosives = expectedExplosives;
    }

    public Integer getUsedExplosives() {
        return usedExplosives;
    }

    public void setUsedExplosives(Integer usedExplosives) {
        this.usedExplosives = usedExplosives;
    }

    public Integer getReturnedExplosives() {
        return returnedExplosives;
    }

    public void setReturnedExplosives(Integer returnedExplosives) {
        this.returnedExplosives = returnedExplosives;
    }

    public boolean isAllReturned() {
        return allReturned;
    }

    public void setAllReturned(boolean allReturned) {
        this.allReturned = allReturned;
    }

    public String getVerificationRemark() {
        return verificationRemark;
    }

    public void setVerificationRemark(String verificationRemark) {
        this.verificationRemark = verificationRemark;
    }

    public LocalDateTime getVerificationTime() {
        return verificationTime;
    }

    public void setVerificationTime(LocalDateTime verificationTime) {
        this.verificationTime = verificationTime;
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
