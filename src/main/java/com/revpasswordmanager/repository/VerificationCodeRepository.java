package com.revpasswordmanager.repository;

import com.revpasswordmanager.model.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findFirstByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(Long userId, String purpose);

    default Optional<VerificationCode> findLatestUnusedByPurpose(Long userId, String purpose) {
        return findFirstByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(userId, purpose);
    }

    @Modifying
    @Transactional
    @Query("UPDATE VerificationCode v SET v.used = true WHERE v.id = :id")
    void markUsed(@Param("id") Long id);
}
