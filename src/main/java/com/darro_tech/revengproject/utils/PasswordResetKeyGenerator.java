package com.darro_tech.revengproject.utils;

import java.security.SecureRandom;

/**
 * Utility class to generate password reset keys that match the NextJS implementation exactly.
 * Uses the same alphabet: 23456789ABCDEFGHIJKLMNPQRSTUVWXYZ (no 1, 0, or O to avoid ambiguity)
 */
public class PasswordResetKeyGenerator {
    
    private static final String ALPHABET = "23456789ABCDEFGHIJKLMNPQRSTUVWXYZ";
    private static final int KEY_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();
    
    /**
     * Generate a 6-character password reset key using the same algorithm as NextJS.
     * Matches the customAlphabet('23456789ABCDEFGHIJKLMNPQRSTUVWXYZ', 6)() implementation.
     * 
     * @return 6-character reset key
     */
    public static String generatePassKey() {
        StringBuilder key = new StringBuilder(KEY_LENGTH);
        
        for (int i = 0; i < KEY_LENGTH; i++) {
            int randomIndex = RANDOM.nextInt(ALPHABET.length());
            key.append(ALPHABET.charAt(randomIndex));
        }
        
        return key.toString();
    }
    
    /**
     * Validate that a reset key follows the correct format.
     * 
     * @param key the key to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidKey(String key) {
        if (key == null || key.length() != KEY_LENGTH) {
            return false;
        }
        
        for (char c : key.toCharArray()) {
            if (ALPHABET.indexOf(c) == -1) {
                return false;
            }
        }
        
        return true;
    }
} 