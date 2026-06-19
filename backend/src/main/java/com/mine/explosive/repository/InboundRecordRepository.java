package com.mine.explosive.repository;

import com.mine.explosive.entity.InboundRecord;
import com.mine.explosive.enums.ExplosiveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InboundRecordRepository extends JpaRepository<InboundRecord, Long> {
    Optional<InboundRecord> findByInboundNo(String inboundNo);
    List<InboundRecord> findByApplicationId(Long applicationId);
    List<InboundRecord> findByBlasterId(Long blasterId);
    List<InboundRecord> findByStorekeeperId(Long storekeeperId);
    List<InboundRecord> findByExplosiveSerialNo(String explosiveSerialNo);

    @Query("SELECT i FROM InboundRecord i WHERE i.inboundTime BETWEEN :start AND :end")
    List<InboundRecord> findByInboundTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(i.returnedQuantity), 0) FROM InboundRecord i WHERE i.application.id = :applicationId AND i.type = :type")
    Integer sumReturnedQuantityByApplicationIdAndType(Long applicationId, ExplosiveType type);

    @Query("SELECT COALESCE(SUM(i.usedQuantity), 0) FROM InboundRecord i WHERE i.application.id = :applicationId AND i.type = :type")
    Integer sumUsedQuantityByApplicationIdAndType(Long applicationId, ExplosiveType type);

    @Query("SELECT i FROM InboundRecord i WHERE i.application.id = :applicationId AND i.type = :type")
    List<InboundRecord> findByApplicationIdAndType(Long applicationId, ExplosiveType type);
}
