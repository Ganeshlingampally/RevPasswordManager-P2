package com.revpasswordmanager.app.repository;

import com.revpasswordmanager.app.entity.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    List<Credential> findByUserUserId(Long userId);

    List<Credential> findByUserUserIdAndCategory(Long userId, String category);

    @Query("SELECT c FROM Credential c WHERE c.user.userId = :userId " +
            "AND (LOWER(c.siteName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.siteUsername) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(c.siteUrl) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Credential> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Query("SELECT c FROM Credential c WHERE c.user.userId = :userId ORDER BY c.siteName ASC")
    List<Credential> findByUserIdSortedBySiteName(@Param("userId") Long userId);

    @Query("SELECT c FROM Credential c WHERE c.user.userId = :userId ORDER BY c.createdAt DESC")
    List<Credential> findByUserIdSortedByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT c FROM Credential c WHERE c.user.userId = :userId ORDER BY c.updatedAt DESC")
    List<Credential> findByUserIdSortedByUpdatedAtDesc(@Param("userId") Long userId);

    long countByUserUserId(Long userId);

    List<Credential> findByUserUserIdAndFavoriteTrue(Long userId);
}
