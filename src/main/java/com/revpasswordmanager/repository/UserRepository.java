package com.revpasswordmanager.repository;

import com.revpasswordmanager.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> mapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setMasterPasswordHash(rs.getString("master_password_hash"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setTwoFaEnabled(rs.getBoolean("two_fa_enabled"));
        user.setTwoFaSecret(rs.getString("two_fa_secret"));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return user;
    };

    public Long save(User user) {
        String sql = "INSERT INTO users (username, master_password_hash, name, email, two_fa_enabled, two_fa_secret, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getMasterPasswordHash());
            ps.setString(3, user.getName());
            ps.setString(4, user.getEmail());
            ps.setBoolean(5, user.isTwoFaEnabled());
            ps.setString(6, user.getTwoFaSecret());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        return jdbcTemplate.query(sql, mapper, username).stream().findFirst();
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.query(sql, mapper, id).stream().findFirst();
    }

    public void updateProfile(Long userId, String name, String email) {
        String sql = "UPDATE users SET name=?, email=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        jdbcTemplate.update(sql, name, email, userId);
    }

    public void updateMasterPassword(Long userId, String newHash) {
        String sql = "UPDATE users SET master_password_hash=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        jdbcTemplate.update(sql, newHash, userId);
    }

    public void updateTwoFa(Long userId, boolean enabled, String secret) {
        String sql = "UPDATE users SET two_fa_enabled=?, two_fa_secret=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
        jdbcTemplate.update(sql, enabled, secret, userId);
    }
}

