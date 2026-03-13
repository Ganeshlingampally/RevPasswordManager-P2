package com.revpasswordmanager.app;

import com.revpasswordmanager.app.config.EncryptionUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class EncryptionUtilTest {

    private static final String SECRET_KEY = "RevSecretKey1234"; // exactly 16 chars

    @Test
    public void testEncryptDecrypt_Success() throws Exception {
        String originalText = "MySuperSecretPassword123!";

        String encrypted = EncryptionUtil.encrypt(originalText, SECRET_KEY);
        assertNotNull(encrypted);
        assertNotEquals(originalText, encrypted);

        String decrypted = EncryptionUtil.decrypt(encrypted, SECRET_KEY);
        assertEquals(originalText, decrypted);
    }

    @Test
    public void testEncrypt_ProducesDifferentOutputFromInput() throws Exception {
        String plainText = "password123";
        String encrypted = EncryptionUtil.encrypt(plainText, SECRET_KEY);

        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
    }

    @Test
    public void testDecrypt_ReturnsOriginalText() throws Exception {
        String plainText = "Hello World!";
        String encrypted = EncryptionUtil.encrypt(plainText, SECRET_KEY);
        String decrypted = EncryptionUtil.decrypt(encrypted, SECRET_KEY);

        assertEquals(plainText, decrypted);
    }

    @Test
    public void testEncrypt_SpecialCharacters() throws Exception {
        String specialChars = "P@$$w0rd!#%^&*()_+-=[]{}|;:',.<>?";
        String encrypted = EncryptionUtil.encrypt(specialChars, SECRET_KEY);
        String decrypted = EncryptionUtil.decrypt(encrypted, SECRET_KEY);

        assertEquals(specialChars, decrypted);
    }

    @Test
    public void testEncrypt_EmptyString() throws Exception {
        String empty = "";
        String encrypted = EncryptionUtil.encrypt(empty, SECRET_KEY);
        String decrypted = EncryptionUtil.decrypt(encrypted, SECRET_KEY);

        assertEquals(empty, decrypted);
    }

    @Test(expected = Exception.class)
    public void testDecrypt_WrongKey() throws Exception {
        String plainText = "secret data";
        String encrypted = EncryptionUtil.encrypt(plainText, SECRET_KEY);


        EncryptionUtil.decrypt(encrypted, "WrongKey12345678");
    }

    @Test
    public void testEncrypt_SameInputDifferentCalls_SameOutput() throws Exception {
        String plainText = "consistent_data";
        String encrypted1 = EncryptionUtil.encrypt(plainText, SECRET_KEY);
        String encrypted2 = EncryptionUtil.encrypt(plainText, SECRET_KEY);


        assertEquals(encrypted1, encrypted2);
    }
}
