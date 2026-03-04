package com.revpasswordmanager.service;

import com.revpasswordmanager.model.User;
import com.revpasswordmanager.repository.UserRepository;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Base64;

@Service
public class TwoFaService {

    private final UserRepository userRepository;

    public TwoFaService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateSecret() {
        byte[] raw = new byte[20];
        new java.security.SecureRandom().nextBytes(raw);
        return Base64.getEncoder().encodeToString(raw);
    }

    public boolean verifyCode(User user, String code) {
        if (!user.isTwoFaEnabled()) {
            return true;
        }
        if (code == null || code.isBlank()) {
            return false;
        }
        long window = Instant.now().getEpochSecond() / 30;
        for (long i = -1; i <= 1; i++) {
            if (generateTotp(user.getTwoFaSecret(), window + i).equals(code)) {
                return true;
            }
        }
        return false;
    }

    public void setTwoFa(Long userId, boolean enabled) {
        if (enabled) {
            userRepository.updateTwoFa(userId, true, generateSecret());
        } else {
            userRepository.updateTwoFa(userId, false, null);
        }
    }

    private String generateTotp(String base64Secret, long timeCounter) {
        try {
            byte[] key = Base64.getDecoder().decode(base64Secret);
            byte[] data = ByteBuffer.allocate(8).putLong(timeCounter).array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hmac = mac.doFinal(data);
            int offset = hmac[hmac.length - 1] & 0x0F;
            int binary = ((hmac[offset] & 0x7F) << 24)
                    | ((hmac[offset + 1] & 0xFF) << 16)
                    | ((hmac[offset + 2] & 0xFF) << 8)
                    | (hmac[offset + 3] & 0xFF);
            int otp = binary % 1000000;
            return String.format("%06d", otp);
        } catch (Exception ex) {
            return "000000";
        }
    }
}

