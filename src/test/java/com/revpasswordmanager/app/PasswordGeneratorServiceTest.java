package com.revpasswordmanager.app;

import com.revpasswordmanager.app.service.PasswordGeneratorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PasswordGeneratorServiceTest {

    private PasswordGeneratorService service;

    @Before
    public void setUp() {
        service = new PasswordGeneratorService();
    }

    @Test
    public void testGenerate_DefaultOptions() {
        Map<String, Object> result = service.generatePassword(16, true, true, true, true);
        assertEquals(16, result.get("password").toString().length());
        assertNotNull(result.get("strength"));
        assertTrue((int) result.get("score") > 0);
    }

    @Test
    public void testGenerate_CustomLength() {
        Map<String, Object> result = service.generatePassword(24, true, true, true, true);
        assertEquals(24, result.get("password").toString().length());
    }

    @Test
    public void testGenerate_OnlyDigits() {
        Map<String, Object> result = service.generatePassword(10, false, false, true, false);
        assertTrue(result.get("password").toString().matches("\\d+"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerate_NoCharTypes() {
        service.generatePassword(10, false, false, false, false);
    }

    @Test
    public void testCheckStrength_Strong() {
        Map<String, Object> result = service.checkStrength("MyStr0ng!P@ss");
        assertTrue((int) result.get("score") >= 60);
    }

    @Test
    public void testCheckStrength_Weak() {
        Map<String, Object> result = service.checkStrength("abc");
        assertTrue((int) result.get("score") < 40);
    }

    @Test
    public void testCalculateScore_Empty() {
        assertEquals(0, service.calculateStrengthScore(""));
    }

    @Test
    public void testCalculateScore_Null() {
        assertEquals(0, service.calculateStrengthScore(null));
    }



    @Test
    public void testGenerate_ExcludeSimilar() {
        Map<String, Object> result = service.generatePassword(20, true, true, true, false, true);
        String password = result.get("password").toString();
        // Similar chars: 0 O 1 l I | `
        assertFalse("Should not contain '0'", password.contains("0"));
        assertFalse("Should not contain 'O'", password.contains("O"));
        assertFalse("Should not contain '1'", password.contains("1"));
        assertFalse("Should not contain 'l'", password.contains("l"));
        assertFalse("Should not contain 'I'", password.contains("I"));
    }

    @Test
    public void testGenerateMultiplePasswords() {
        java.util.List<Map<String, Object>> results = service.generateMultiplePasswords(
                5, 16, true, true, true, true, false);
        assertEquals(5, results.size());
        for (Map<String, Object> r : results) {
            assertNotNull(r.get("password"));
            assertEquals(16, r.get("password").toString().length());
            assertNotNull(r.get("strength"));

        }
    }

    @Test
    public void testGenerateMultiplePasswords_MaxCapped() {
        // Should cap at 10 even if 20 is requested
        java.util.List<Map<String, Object>> results = service.generateMultiplePasswords(
                20, 12, true, true, true, true, false);
        assertEquals(10, results.size());
    }
}
