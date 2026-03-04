package com.revpasswordmanager.app;

import com.revpasswordmanager.app.dto.*;
import com.revpasswordmanager.app.entity.SecurityQuestion;
import com.revpasswordmanager.app.entity.User;
import com.revpasswordmanager.app.entity.VerificationCode;
import com.revpasswordmanager.app.exception.ResourceNotFoundException;
import com.revpasswordmanager.app.repository.SecurityQuestionRepository;
import com.revpasswordmanager.app.repository.UserRepository;
import com.revpasswordmanager.app.repository.VerificationCodeRepository;
import com.revpasswordmanager.app.service.UserService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private SecurityQuestionRepository securityQuestionRepository;
    @Mock
    private VerificationCodeRepository verificationCodeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserRequestDTO requestDTO;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(userService, "codeExpiryMinutes", 15);

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("ganesh");
        testUser.setEmail("ganesh@test.com");
        testUser.setMasterPasswordHash("$2a$12$hashed");
        testUser.setTwoFactorEnabled(false);

        requestDTO = new UserRequestDTO();
        requestDTO.setUsername("ganesh");
        requestDTO.setEmail("ganesh@test.com");
        requestDTO.setMasterPassword("Password123!");
    }

    @Test
    public void testRegister_Success() {
        when(userRepository.existsByUsername("ganesh")).thenReturn(false);
        when(userRepository.existsByEmail("ganesh@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$12$hashed");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDTO result = userService.register(requestDTO);
        assertNotNull(result);
        assertEquals("ganesh", result.getUsername());
        verify(passwordEncoder).encode("Password123!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRegister_DuplicateUsername() {
        when(userRepository.existsByUsername("ganesh")).thenReturn(true);
        userService.register(requestDTO);
    }

    @Test
    public void testLogin_Success() {
        when(userRepository.findByUsername("ganesh")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", "$2a$12$hashed")).thenReturn(true);

        UserResponseDTO result = userService.login(requestDTO);
        assertEquals("ganesh", result.getUsername());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLogin_WrongPassword() {
        when(userRepository.findByUsername("ganesh")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        userService.login(requestDTO);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testLogin_UserNotFound() {
        when(userRepository.findByUsername("ganesh")).thenReturn(Optional.empty());
        userService.login(requestDTO);
    }

    @Test
    public void testToggleTwoFactor() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        UserResponseDTO result = userService.toggleTwoFactor(1L);
        assertNotNull(result);
    }

    @Test
    public void testSaveSecurityQuestions_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("$hashed$");
        when(securityQuestionRepository.save(any(SecurityQuestion.class)))
                .thenAnswer(inv -> {
                    SecurityQuestion sq = inv.getArgument(0);
                    sq.setQuestionId(1L);
                    return sq;
                });

        List<SecurityQuestionDTO> questions = Arrays.asList(
                makeQuestion("Pet name?", "fluffy"),
                makeQuestion("Birth city?", "chennai"),
                makeQuestion("Fav color?", "blue"));

        List<SecurityQuestionDTO> result = userService.saveSecurityQuestions(1L, questions);
        assertEquals(3, result.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveSecurityQuestions_LessThanThree() {
        List<SecurityQuestionDTO> questions = Arrays.asList(
                makeQuestion("Q1?", "A1"), makeQuestion("Q2?", "A2"));
        userService.saveSecurityQuestions(1L, questions);
    }

    @Test
    public void testGenerateVerificationCode() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(verificationCodeRepository.save(any(VerificationCode.class)))
                .thenAnswer(inv -> {
                    VerificationCode vc = inv.getArgument(0);
                    vc.setCodeId(1L);
                    return vc;
                });

        Map<String, Object> result = userService.generateVerificationCode(1L, "PASSWORD_RESET");
        assertNotNull(result.get("code"));
        assertEquals(6, result.get("code").toString().length());
    }

    @Test
    public void testValidateVerificationCode_Success() {
        VerificationCode vc = new VerificationCode(testUser, "123456", "RESET", LocalDateTime.now().plusMinutes(10));
        vc.setCodeId(1L);
        when(verificationCodeRepository.findByUserUserIdAndCodeAndPurposeAndUsedFalse(1L, "123456", "RESET"))
                .thenReturn(Optional.of(vc));
        when(verificationCodeRepository.save(any())).thenReturn(vc);

        Map<String, Object> result = userService.validateVerificationCode(1L, "123456", "RESET");
        assertEquals(true, result.get("valid"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateVerificationCode_Expired() {
        VerificationCode vc = new VerificationCode(testUser, "123456", "RESET", LocalDateTime.now().minusMinutes(5));
        when(verificationCodeRepository.findByUserUserIdAndCodeAndPurposeAndUsedFalse(1L, "123456", "RESET"))
                .thenReturn(Optional.of(vc));
        userService.validateVerificationCode(1L, "123456", "RESET");
    }

    private SecurityQuestionDTO makeQuestion(String q, String a) {
        SecurityQuestionDTO dto = new SecurityQuestionDTO();
        dto.setQuestion(q);
        dto.setAnswer(a);
        return dto;
    }



    @Test
    public void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        UserResponseDTO result = userService.getUserById(1L);
        assertNotNull(result);
        assertEquals("ganesh", result.getUsername());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        userService.getUserById(999L);
    }

    @Test
    public void testUpdateProfile_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserRequestDTO updateDTO = new UserRequestDTO();
        updateDTO.setFirstName("Ganesh");
        updateDTO.setLastName("Kumar");
        updateDTO.setEmail("ganesh_new@test.com");

        UserResponseDTO result = userService.updateProfile(1L, updateDTO);
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testChangeMasterPassword_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("Password123!", "$2a$12$hashed")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword456!")).thenReturn("$2a$12$newhashed");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDTO result = userService.changeMasterPassword(1L, "Password123!", "NewPassword456!");
        assertNotNull(result);
        verify(passwordEncoder).encode("NewPassword456!");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChangeMasterPassword_WrongCurrent() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("WrongPass!", "$2a$12$hashed")).thenReturn(false);

        userService.changeMasterPassword(1L, "WrongPass!", "NewPassword456!");
    }



    @Test
    public void testGetSecurityQuestionsByUsername_Success() {
        SecurityQuestion sq1 = new SecurityQuestion(testUser, "Pet name?", "$hashed$");
        sq1.setQuestionId(1L);
        SecurityQuestion sq2 = new SecurityQuestion(testUser, "Birth city?", "$hashed$");
        sq2.setQuestionId(2L);
        SecurityQuestion sq3 = new SecurityQuestion(testUser, "Fav color?", "$hashed$");
        sq3.setQuestionId(3L);

        when(userRepository.findByUsername("ganesh")).thenReturn(Optional.of(testUser));
        when(securityQuestionRepository.findByUserUserId(1L))
                .thenReturn(Arrays.asList(sq1, sq2, sq3));

        List<SecurityQuestionDTO> result = userService.getSecurityQuestionsByUsername("ganesh");
        assertEquals(3, result.size());
        assertEquals("Pet name?", result.get(0).getQuestion());
        assertNull(result.get(0).getAnswer()); // answer should not be exposed
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetSecurityQuestionsByUsername_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        userService.getSecurityQuestionsByUsername("nonexistent");
    }

    @Test
    public void testVerifySecurityAnswers_Success() {
        SecurityQuestion sq1 = new SecurityQuestion(testUser, "Pet name?", "$hashed1$");
        sq1.setQuestionId(1L);
        SecurityQuestion sq2 = new SecurityQuestion(testUser, "Birth city?", "$hashed2$");
        sq2.setQuestionId(2L);
        SecurityQuestion sq3 = new SecurityQuestion(testUser, "Fav color?", "$hashed3$");
        sq3.setQuestionId(3L);

        when(userRepository.findByUsername("ganesh")).thenReturn(Optional.of(testUser));
        when(securityQuestionRepository.findByUserUserId(1L))
                .thenReturn(Arrays.asList(sq1, sq2, sq3));
        when(passwordEncoder.matches("fluffy", "$hashed1$")).thenReturn(true);
        when(passwordEncoder.matches("chennai", "$hashed2$")).thenReturn(true);
        when(passwordEncoder.matches("blue", "$hashed3$")).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(verificationCodeRepository.save(any(VerificationCode.class)))
                .thenAnswer(inv -> {
                    VerificationCode vc = inv.getArgument(0);
                    vc.setCodeId(1L);
                    return vc;
                });

        List<SecurityQuestionDTO> answers = Arrays.asList(
                makeQuestionWithId(1L, "fluffy"),
                makeQuestionWithId(2L, "chennai"),
                makeQuestionWithId(3L, "blue"));

        Map<String, Object> result = userService.verifySecurityAnswers("ganesh", answers);
        assertNotNull(result.get("code"));
        assertEquals(1L, result.get("userId"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySecurityAnswers_WrongAnswer() {
        SecurityQuestion sq1 = new SecurityQuestion(testUser, "Pet name?", "$hashed1$");
        sq1.setQuestionId(1L);
        SecurityQuestion sq2 = new SecurityQuestion(testUser, "Birth city?", "$hashed2$");
        sq2.setQuestionId(2L);
        SecurityQuestion sq3 = new SecurityQuestion(testUser, "Fav color?", "$hashed3$");
        sq3.setQuestionId(3L);

        when(userRepository.findByUsername("ganesh")).thenReturn(Optional.of(testUser));
        when(securityQuestionRepository.findByUserUserId(1L))
                .thenReturn(Arrays.asList(sq1, sq2, sq3));
        when(passwordEncoder.matches("wronganswer", "$hashed1$")).thenReturn(false);

        List<SecurityQuestionDTO> answers = Arrays.asList(
                makeQuestionWithId(1L, "wronganswer"),
                makeQuestionWithId(2L, "chennai"),
                makeQuestionWithId(3L, "blue"));

        userService.verifySecurityAnswers("ganesh", answers);
    }

    @Test
    public void testResetPasswordWithOTP_Success() {
        VerificationCode vc = new VerificationCode(testUser, "123456", "PASSWORD_RESET",
                LocalDateTime.now().plusMinutes(10));
        vc.setCodeId(1L);

        when(userRepository.findByUsername("ganesh")).thenReturn(Optional.of(testUser));
        when(verificationCodeRepository.findByUserUserIdAndCodeAndPurposeAndUsedFalse(
                1L, "123456", "PASSWORD_RESET")).thenReturn(Optional.of(vc));
        when(verificationCodeRepository.save(any())).thenReturn(vc);
        when(passwordEncoder.encode("NewSecurePass1!")).thenReturn("$2a$12$newHash");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDTO result = userService.resetPasswordWithOTP("ganesh", "123456", "NewSecurePass1!");
        assertNotNull(result);
        verify(passwordEncoder).encode("NewSecurePass1!");
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testResetPasswordWithOTP_InvalidOTP() {
        when(userRepository.findByUsername("ganesh")).thenReturn(Optional.of(testUser));
        when(verificationCodeRepository.findByUserUserIdAndCodeAndPurposeAndUsedFalse(
                1L, "999999", "PASSWORD_RESET")).thenReturn(Optional.empty());

        userService.resetPasswordWithOTP("ganesh", "999999", "NewSecurePass1!");
    }

    private SecurityQuestionDTO makeQuestionWithId(Long id, String answer) {
        SecurityQuestionDTO dto = new SecurityQuestionDTO();
        dto.setQuestionId(id);
        dto.setAnswer(answer);
        return dto;
    }
}