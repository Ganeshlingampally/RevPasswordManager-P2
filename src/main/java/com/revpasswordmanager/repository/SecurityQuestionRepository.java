package com.revpasswordmanager.repository;

import com.revpasswordmanager.model.SecurityQuestion;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SecurityQuestionRepository {

    private final JdbcTemplate jdbcTemplate;

    public SecurityQuestionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SecurityQuestion> mapper = (rs, rowNum) -> {
        SecurityQuestion q = new SecurityQuestion();
        q.setId(rs.getLong("id"));
        q.setUserId(rs.getLong("user_id"));
        q.setQuestion(rs.getString("question"));
        q.setAnswerHash(rs.getString("answer_hash"));
        q.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return q;
    };

    public void saveAll(Long userId, List<SecurityQuestion> questions) {
        String sql = "INSERT INTO security_questions (user_id, question, answer_hash, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        for (SecurityQuestion q : questions) {
            jdbcTemplate.update(sql, userId, q.getQuestion(), q.getAnswerHash());
        }
    }

    public List<SecurityQuestion> findByUserId(Long userId) {
        return jdbcTemplate.query("SELECT * FROM security_questions WHERE user_id=?", mapper, userId);
    }

    public void replaceAll(Long userId, List<SecurityQuestion> questions) {
        jdbcTemplate.update("DELETE FROM security_questions WHERE user_id=?", userId);
        saveAll(userId, questions);
    }
}

