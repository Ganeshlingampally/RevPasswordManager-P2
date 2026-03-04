package com.revpasswordmanager.repository;

import com.revpasswordmanager.model.Credential;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, Long> {

    @Query("SELECT c FROM Credential c WHERE c.userId = :userId " +
            "AND (:q IS NULL OR :q = '' OR LOWER(c.accountName) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.username) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(c.url) LIKE LOWER(CONCAT('%', :q, '%'))) "
            +
            "AND (:category IS NULL OR :category = '' OR c.category = :category) " +
            "AND (:favoritesOnly = false OR c.favorite = true)")
    List<Credential> searchWithDynamicSort(@Param("userId") Long userId, @Param("q") String q,
            @Param("category") String category, @Param("favoritesOnly") boolean favoritesOnly, Sort sort);

    default List<Credential> search(Long userId, String q, String category, String sortBy, boolean favoritesOnly) {
        Sort sort = Sort.by(Sort.Direction.DESC, "updatedAt");
        if ("name".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "accountName");
        } else if ("created".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        return searchWithDynamicSort(userId, q, category, favoritesOnly, sort);
    }

    Optional<Credential> findByIdAndUserId(Long id, Long userId);

    default void update(Credential c) {
        save(c);
    }

    @Transactional
    void deleteByIdAndUserId(Long id, Long userId);

    default void delete(Long id, Long userId) {
        deleteByIdAndUserId(id, userId);
    }

    @Modifying
    @Transactional
    @Query("UPDATE Credential c SET c.lastAccessedAt = CURRENT_TIMESTAMP WHERE c.id = :id AND c.userId = :userId")
    void markAccessed(@Param("id") Long id, @Param("userId") Long userId);

    long countByUserId(Long userId);

    default int countAll(Long userId) {
        return (int) countByUserId(userId);
    }

    long countByUserIdAndPasswordStrength(Long userId, String strength);

    default int countWeak(Long userId) {
        return (int) countByUserIdAndPasswordStrength(userId, "WEAK");
    }

    @Query("SELECT count(c) FROM Credential c WHERE c.userId = :userId AND c.updatedAt > :cutoff")
    long countRecentByCutoff(@Param("userId") Long userId, @Param("cutoff") LocalDateTime cutoff);

    default int countRecent(Long userId, int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return (int) countRecentByCutoff(userId, cutoff);
    }
}
