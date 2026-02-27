package com.revpasswordmanager.repository;

import com.revpasswordmanager.model.VerificationCode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class VerificationCodeRepository {

    private final JdbcTemplate jdbcTemplate;

    public VerificationCodeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<VerificationCode> mapper = (rs, rowNum) -> {
        VerificationCode code = new VerificationCode();
        code.setId(rs.getLong("id"));
        code.setUserId(rs.getLong("user_id"));
        code.setCode(rs.getString("code"));
        code.setPurpose(rs.getString("purpose"));
        code.setExpiryTime(rs.getTimestamp("expiry_time").toLocalDateTime());
        code.setUsed(rs.getBoolean("is_used"));
        code.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return code;
    };

    public void save(VerificationCode verificationCode) {
        String sql = "INSERT INTO verification_codes (user_id, code, purpose, expiry_time, is_used, created_at) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        jdbcTemplate.update(sql, verificationCode.getUserId(), verificationCode.getCode(), verificationCode.getPurpose(), java.sql.Timestamp.valueOf(verificationCode.getExpiryTime()), verificationCode.isUsed());
    }

    public Optional<VerificationCode> findLatestUnusedByPurpose(Long userId, String purpose) {
        String sql = "SELECT * FROM verification_codes WHERE user_id=? AND purpose=? AND is_used=0 ORDER BY created_at DESC FETCH FIRST 1 ROWS ONLY";
        return jdbcTemplate.query(sql, mapper, userId, purpose).stream().findFirst();
    }

    public void markUsed(Long id) {
        jdbcTemplate.update("UPDATE verification_codes SET is_used=1 WHERE id=?", id);
    }
}

