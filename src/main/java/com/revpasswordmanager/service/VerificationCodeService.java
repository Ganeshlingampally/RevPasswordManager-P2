package com.revpasswordmanager.service;

import com.revpasswordmanager.model.VerificationCode;
import com.revpasswordmanager.repository.VerificationCodeRepository;
import com.revpasswordmanager.util.VerificationCodeGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class VerificationCodeService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final VerificationCodeGenerator verificationCodeGenerator;
    private final int ttlMinutes;

    public VerificationCodeService(VerificationCodeRepository verificationCodeRepository,
                                   VerificationCodeGenerator verificationCodeGenerator,
                                   @Value("${app.security.verification-code-ttl-minutes}") int ttlMinutes) {
        this.verificationCodeRepository = verificationCodeRepository;
        this.verificationCodeGenerator = verificationCodeGenerator;
        this.ttlMinutes = ttlMinutes;
    }

    public String issueCode(Long userId, String purpose) {
        String code = verificationCodeGenerator.generate6DigitCode();
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setUserId(userId);
        verificationCode.setCode(code);
        verificationCode.setPurpose(purpose);
        verificationCode.setExpiryTime(LocalDateTime.now().plusMinutes(ttlMinutes));
        verificationCode.setUsed(false);
        verificationCodeRepository.save(verificationCode);
        return code;
    }

    public boolean verify(Long userId, String purpose, String codeInput) {
        return verificationCodeRepository.findLatestUnusedByPurpose(userId, purpose)
                .filter(v -> v.getExpiryTime().isAfter(LocalDateTime.now()))
                .filter(v -> v.getCode().equals(codeInput))
                .map(v -> {
                    verificationCodeRepository.markUsed(v.getId());
                    return true;
                })
                .orElse(false);
    }
}

