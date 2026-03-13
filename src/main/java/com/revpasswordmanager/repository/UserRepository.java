package com.revpasswordmanager.repository;

import com.revpasswordmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.name = :name, u.email = :email, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updateProfile(@Param("userId") Long userId, @Param("name") String name, @Param("email") String email);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.masterPasswordHash = :newHash, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updateMasterPassword(@Param("userId") Long userId, @Param("newHash") String newHash);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.twoFaEnabled = :enabled, u.twoFaSecret = :secret, u.updatedAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updateTwoFa(@Param("userId") Long userId, @Param("enabled") boolean enabled, @Param("secret") String secret);
}
