package com.revpasswordmanager.app.repository;

import com.revpasswordmanager.app.entity.SecurityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityQuestionRepository extends JpaRepository<SecurityQuestion, Long> {

    List<SecurityQuestion> findByUserUserId(Long userId);

    long countByUserUserId(Long userId);

    void deleteByUserUserId(Long userId);
}
