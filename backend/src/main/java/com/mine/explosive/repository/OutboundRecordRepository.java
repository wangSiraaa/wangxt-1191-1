package com.mine.explosive.repository;

import com.mine.explosive.entity.OutboundRecord;
import com.mine.explosive.enums.ExplosiveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OutboundRecordRepository extends JpaRepository<OutboundRecord, Long> {
    Optional<OutboundRecord> findByOutboundNo(String outboundNo);
    List<OutboundRecord> findByApplicationId(Long applicationId);
    List<OutboundRecord> findByBlasterId(Long blasterId);
    List<OutboundRecord> findByStorekeeperId(Long storekeeperId);
    List<OutboundRecord> findByExplosiveSerialNo(String explosiveSerialNo);

    @Query("SELECT o FROM OutboundRecord o WHERE o.outboundTime BETWEEN :start AND :end")
    List<OutboundRecord> findByOutboundTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.quantity), 0) FROM OutboundRecord o WHERE o.application.id = :applicationId AND o.type = :type")
    Integer sumQuantityByApplicationIdAndType(Long applicationId, ExplosiveType type);

    @Query("SELECT o FROM OutboundRecord o WHERE o.application.id = :applicationId AND o.type = :type")
    List<OutboundRecord> findByApplicationIdAndType(Long applicationId, ExplosiveType type);
}
