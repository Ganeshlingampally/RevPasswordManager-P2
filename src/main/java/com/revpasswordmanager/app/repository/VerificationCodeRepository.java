package com.revpasswordmanager.app.repository;

import com.revpasswordmanager.app.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findByUserUserIdAndCodeAndPurposeAndUsedFalse(
            Long userId, String code, String purpose);

    List<VerificationCode> findByUserUserIdAndPurposeAndUsedFalse(Long userId, String purpose);

    List<VerificationCode> findByExpiresAtBeforeAndUsedFalse(LocalDateTime now);

    void deleteByExpiresAtBefore(LocalDateTime now);
}
