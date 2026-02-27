package com.revpasswordmanager.repository;

import com.revpasswordmanager.model.Credential;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class CredentialRepository {

    private final JdbcTemplate jdbcTemplate;

    public CredentialRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Credential> mapper = (rs, rowNum) -> {
        Credential c = new Credential();
        c.setId(rs.getLong("id"));
        c.setUserId(rs.getLong("user_id"));
        c.setAccountName(rs.getString("account_name"));
        c.setUsername(rs.getString("username"));
        c.setEncryptedPassword(rs.getString("encrypted_password"));
        c.setUrl(rs.getString("url"));
        c.setNotes(rs.getString("notes"));
        c.setCategory(rs.getString("category"));
        c.setFavorite(rs.getBoolean("favorite"));
        c.setPasswordStrength(rs.getString("password_strength"));
        c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        c.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        if (rs.getTimestamp("last_accessed_at") != null) {
            c.setLastAccessedAt(rs.getTimestamp("last_accessed_at").toLocalDateTime());
        }
        return c;
    };

    public Long save(Credential c) {
        String sql = "INSERT INTO credentials (user_id, account_name, username, encrypted_password, url, notes, category, favorite, password_strength, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, c.getUserId());
            ps.setString(2, c.getAccountName());
            ps.setString(3, c.getUsername());
            ps.setString(4, c.getEncryptedPassword());
            ps.setString(5, c.getUrl());
            ps.setString(6, c.getNotes());
            ps.setString(7, c.getCategory());
            ps.setBoolean(8, c.isFavorite());
            ps.setString(9, c.getPasswordStrength());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public List<Credential> search(Long userId, String q, String category, String sortBy, boolean favoritesOnly) {
        StringBuilder sql = new StringBuilder("SELECT * FROM credentials WHERE user_id=?");
        if (q != null && !q.isBlank()) {
            sql.append(" AND (LOWER(account_name) LIKE ? OR LOWER(username) LIKE ? OR LOWER(url) LIKE ?)");
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND category=?");
        }
        if (favoritesOnly) {
            sql.append(" AND favorite=1");
        }

        String order = "updated_at DESC";
        if ("name".equalsIgnoreCase(sortBy)) {
            order = "account_name ASC";
        } else if ("created".equalsIgnoreCase(sortBy)) {
            order = "created_at DESC";
        }
        sql.append(" ORDER BY ").append(order);

        if (q != null && !q.isBlank()) {
            String like = "%" + q.toLowerCase() + "%";
            if (category != null && !category.isBlank()) {
                return jdbcTemplate.query(sql.toString(), mapper, userId, like, like, like, category);
            }
            return jdbcTemplate.query(sql.toString(), mapper, userId, like, like, like);
        }
        if (category != null && !category.isBlank()) {
            return jdbcTemplate.query(sql.toString(), mapper, userId, category);
        }
        return jdbcTemplate.query(sql.toString(), mapper, userId);
    }

    public Optional<Credential> findByIdAndUserId(Long id, Long userId) {
        return jdbcTemplate.query("SELECT * FROM credentials WHERE id=? AND user_id=?", mapper, id, userId).stream().findFirst();
    }

    public void update(Credential c) {
        String sql = "UPDATE credentials SET account_name=?, username=?, encrypted_password=?, url=?, notes=?, category=?, favorite=?, password_strength=?, updated_at=CURRENT_TIMESTAMP WHERE id=? AND user_id=?";
        jdbcTemplate.update(sql, c.getAccountName(), c.getUsername(), c.getEncryptedPassword(), c.getUrl(), c.getNotes(), c.getCategory(), c.isFavorite(), c.getPasswordStrength(), c.getId(), c.getUserId());
    }

    public void delete(Long id, Long userId) {
        jdbcTemplate.update("DELETE FROM credentials WHERE id=? AND user_id=?", id, userId);
    }

    public void markAccessed(Long id, Long userId) {
        jdbcTemplate.update("UPDATE credentials SET last_accessed_at=CURRENT_TIMESTAMP WHERE id=? AND user_id=?", id, userId);
    }

    public int countAll(Long userId) {
        Integer val = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM credentials WHERE user_id=?", Integer.class, userId);
        return val == null ? 0 : val;
    }

    public int countWeak(Long userId) {
        Integer val = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM credentials WHERE user_id=? AND password_strength='WEAK'", Integer.class, userId);
        return val == null ? 0 : val;
    }

    public int countRecent(Long userId, int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        Integer val = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM credentials WHERE user_id=? AND updated_at > ?",
                Integer.class,
                userId,
                Timestamp.valueOf(cutoff));
        return val == null ? 0 : val;
    }
}

