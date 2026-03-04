package com.revpasswordmanager.repository;

import com.revpasswordmanager.model.SecurityQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SecurityQuestionRepository extends JpaRepository<SecurityQuestion, Long> {

    List<SecurityQuestion> findByUserId(Long userId);

    @Transactional
    void deleteByUserId(Long userId);

    default void saveAll(Long userId, List<SecurityQuestion> questions) {
        for (SecurityQuestion q : questions) {
            q.setUserId(userId);
        }
        saveAll((Iterable<SecurityQuestion>) questions);
    }

    default void replaceAll(Long userId, List<SecurityQuestion> questions) {
        deleteByUserId(userId);
        saveAll(userId, questions);
    }
}
