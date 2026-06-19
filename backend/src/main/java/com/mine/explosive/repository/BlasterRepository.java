package com.mine.explosive.repository;

import com.mine.explosive.entity.Blaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlasterRepository extends JpaRepository<Blaster, Long> {
    Optional<Blaster> findByUserId(Long userId);
    Optional<Blaster> findByLicenseNo(String licenseNo);

    @Query("SELECT b FROM Blaster b WHERE b.licenseExpiryDate <= :expiryDate")
    List<Blaster> findBlastersWithExpiringLicense(LocalDate expiryDate);

    boolean existsByLicenseNo(String licenseNo);
}
