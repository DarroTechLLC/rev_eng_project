package com.darro_tech.revengproject.config;  
  
import org.springframework.context.annotation.Bean;  
import org.springframework.context.annotation.Configuration;  
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;  
import org.springframework.security.crypto.password.PasswordEncoder;  
  
/**   
 * Configuration class for password encoding.  
 */  
@Configuration  
public class PasswordEncoderConfig {  
  
    /**   
     * Creates a PasswordEncoder bean using BCrypt hashing algorithm.  
     *   
     * @return the password encoder bean  
     */  
    @Bean  
    public PasswordEncoder passwordEncoder() {  
        return new BCryptPasswordEncoder();  
    }  
} 
