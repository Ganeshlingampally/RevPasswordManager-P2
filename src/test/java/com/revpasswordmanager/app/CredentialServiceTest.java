package com.revpasswordmanager.app;

import com.revpasswordmanager.app.config.EncryptionUtil;
import com.revpasswordmanager.app.dto.CredentialRequestDTO;
import com.revpasswordmanager.app.dto.CredentialResponseDTO;
import com.revpasswordmanager.app.entity.Credential;
import com.revpasswordmanager.app.entity.User;
import com.revpasswordmanager.app.exception.ResourceNotFoundException;
import com.revpasswordmanager.app.repository.CredentialRepository;
import com.revpasswordmanager.app.repository.UserRepository;
import com.revpasswordmanager.app.service.CredentialService;
import com.revpasswordmanager.app.service.PasswordGeneratorService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CredentialServiceTest {

    @Mock
    private CredentialRepository credentialRepository;
    @Mock
    private UserRepository userRepository;
    @Spy
    private PasswordGeneratorService passwordGeneratorService = new PasswordGeneratorService();
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private CredentialService credentialService;

    private static final String SECRET_KEY = "RevSecretKey1234";
    private User testUser;
    private Credential testCredential;
    private CredentialRequestDTO requestDTO;

    @Before
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(credentialService, "secretKey", SECRET_KEY);

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("ganesh");

        testCredential = new Credential();
        testCredential.setCredentialId(1L);
        testCredential.setUser(testUser);
        testCredential.setSiteName("GitHub");
        testCredential.setSiteUrl("https://github.com");
        testCredential.setSiteUsername("ganesh_dev");
        testCredential.setEncryptedPassword(EncryptionUtil.encrypt("mypassword", SECRET_KEY));
        testCredential.setCategory("Development");

        requestDTO = new CredentialRequestDTO();
        requestDTO.setSiteName("GitHub");
        requestDTO.setSiteUrl("https://github.com");
        requestDTO.setSiteUsername("ganesh_dev");
        requestDTO.setPassword("mypassword");
        requestDTO.setCategory("Development");
    }

    @Test
    public void testCreateCredential() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(credentialRepository.save(any(Credential.class))).thenReturn(testCredential);

        CredentialResponseDTO result = credentialService.createCredential(1L, requestDTO);
        assertNotNull(result);
        assertEquals("GitHub", result.getSiteName());
        assertEquals("mypassword", result.getDecryptedPassword());
    }

    @Test
    public void testGetAllCredentials() {
        when(credentialRepository.findByUserUserId(1L)).thenReturn(Arrays.asList(testCredential));
        List<CredentialResponseDTO> result = credentialService.getAllCredentials(1L);
        assertEquals(1, result.size());
        assertEquals("GitHub", result.get(0).getSiteName());
    }

    @Test
    public void testGetCredentialById() {
        when(credentialRepository.findById(1L)).thenReturn(Optional.of(testCredential));
        CredentialResponseDTO result = credentialService.getCredentialById(1L, 1L);
        assertEquals(Long.valueOf(1L), result.getCredentialId());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetCredentialById_NotFound() {
        when(credentialRepository.findById(999L)).thenReturn(Optional.empty());
        credentialService.getCredentialById(999L, 1L);
    }

    @Test
    public void testDeleteCredential() {
        when(credentialRepository.findById(1L)).thenReturn(Optional.of(testCredential));
        credentialService.deleteCredential(1L, 1L);
        verify(credentialRepository).delete(testCredential);
    }

    @Test
    public void testSearchCredentials() {
        when(credentialRepository.searchByKeyword(1L, "git")).thenReturn(Arrays.asList(testCredential));
        List<CredentialResponseDTO> result = credentialService.searchCredentials(1L, "git");
        assertEquals(1, result.size());
    }

    @Test
    public void testFilterByCategory() {
        when(credentialRepository.findByUserUserIdAndCategory(1L, "Development"))
                .thenReturn(Arrays.asList(testCredential));
        List<CredentialResponseDTO> result = credentialService.filterByCategory(1L, "Development");
        assertEquals(1, result.size());
    }

    @Test
    public void testSortByName() {
        when(credentialRepository.findByUserIdSortedBySiteName(1L)).thenReturn(Arrays.asList(testCredential));
        List<CredentialResponseDTO> result = credentialService.sortCredentials(1L, "name");
        assertEquals(1, result.size());
    }



    @Test
    public void testToggleFavorite() {
        testCredential.setFavorite(false);
        when(credentialRepository.findById(1L)).thenReturn(Optional.of(testCredential));
        when(credentialRepository.save(any(Credential.class))).thenReturn(testCredential);

        CredentialResponseDTO result = credentialService.toggleFavorite(1L, 1L);
        assertNotNull(result);
        verify(credentialRepository).save(any(Credential.class));
    }

    @Test
    public void testGetFavorites() {
        testCredential.setFavorite(true);
        when(credentialRepository.findByUserUserIdAndFavoriteTrue(1L))
                .thenReturn(Arrays.asList(testCredential));

        List<CredentialResponseDTO> result = credentialService.getFavorites(1L);
        assertEquals(1, result.size());
        assertEquals("GitHub", result.get(0).getSiteName());
    }

    @Test
    public void testUpdateCredential() {
        when(credentialRepository.findById(1L)).thenReturn(Optional.of(testCredential));
        when(credentialRepository.save(any(Credential.class))).thenReturn(testCredential);

        requestDTO.setFavorite(true);
        CredentialResponseDTO result = credentialService.updateCredential(1L, 1L, requestDTO);
        assertNotNull(result);
        verify(credentialRepository).save(any(Credential.class));
    }
}
