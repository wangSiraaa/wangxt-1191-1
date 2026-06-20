package com.mine.explosive.repository;

import com.mine.explosive.entity.Explosive;
import com.mine.explosive.enums.ExplosiveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExplosiveRepository extends JpaRepository<Explosive, Long> {
    Optional<Explosive> findBySerialNo(String serialNo);
    List<Explosive> findByType(ExplosiveType type);

    @Query("SELECT e FROM Explosive e WHERE e.type = :type AND e.availableQuantity > 0")
    List<Explosive> findAvailableByType(ExplosiveType type);

    @Query("SELECT SUM(e.availableQuantity) FROM Explosive e WHERE e.type = :type")
    Integer sumAvailableQuantityByType(ExplosiveType type);

    boolean existsBySerialNo(String serialNo);

    long countByTypeAndAvailableQuantityGreaterThan(ExplosiveType type, Integer quantity);

    @Query("SELECT COUNT(e) > 0 FROM Explosive e WHERE e.serialNo = :serialNo AND e.availableQuantity >= :quantity")
    boolean hasSufficientQuantity(String serialNo, Integer quantity);
}
