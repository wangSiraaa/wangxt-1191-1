package com.mine.explosive.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mine.explosive.enums.ExplosiveType;
import jakarta.persistence.*;


import java.time.LocalDateTime;


@Entity
@Table(name = "outbound_record")
public class OutboundRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String outboundNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private PickupApplication application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "explosive_id", nullable = false)
    private Explosive explosive;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExplosiveType type;

    @Column(nullable = false)
    private String explosiveSerialNo;

    @Column(nullable = false)
    private Integer quantity;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storekeeper_id", nullable = false)
    private User storekeeper;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blaster_id", nullable = false)
    private User blaster;

    @Column(nullable = false)
    private LocalDateTime outboundTime;

    private String workFace;

    private String remarks;

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

    public String getOutboundNo() {
        return outboundNo;
    }

    public void setOutboundNo(String outboundNo) {
        this.outboundNo = outboundNo;
    }

    public PickupApplication getApplication() {
        return application;
    }

    public void setApplication(PickupApplication application) {
        this.application = application;
    }

    public Explosive getExplosive() {
        return explosive;
    }

    public void setExplosive(Explosive explosive) {
        this.explosive = explosive;
    }

    public ExplosiveType getType() {
        return type;
    }

    public void setType(ExplosiveType type) {
        this.type = type;
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

    public User getStorekeeper() {
        return storekeeper;
    }

    public void setStorekeeper(User storekeeper) {
        this.storekeeper = storekeeper;
    }

    public User getBlaster() {
        return blaster;
    }

    public void setBlaster(User blaster) {
        this.blaster = blaster;
    }

    public LocalDateTime getOutboundTime() {
        return outboundTime;
    }

    public void setOutboundTime(LocalDateTime outboundTime) {
        this.outboundTime = outboundTime;
    }

    public String getWorkFace() {
        return workFace;
    }

    public void setWorkFace(String workFace) {
        this.workFace = workFace;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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
