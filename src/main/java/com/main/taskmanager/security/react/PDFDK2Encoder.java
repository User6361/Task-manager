package com.main.taskmanager.security.react;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

@Component
public class PDFDK2Encoder implements PasswordEncoder {

    @Value("${app.jwt.password.encoder.secret}")
    private String secret;
    @Value("${app.jwt.password.encoder.iteration}")
    private Integer iteration;
    @Value("${app.jwt.password.encoder.keyLenght}")
    private Integer keyLength;

    private static final String SECRET_KEY_INSTANCE = "PBKDF2withHmacSHA512";

    @Override
    public @Nullable String encode(@Nullable CharSequence rawPassword) {
        try{
            if (rawPassword == null) {
                throw new IllegalArgumentException("Raw password cannot be null");
            }
            byte[] result = SecretKeyFactory.getInstance(SECRET_KEY_INSTANCE)
                    .generateSecret(new PBEKeySpec(rawPassword.toString().toCharArray(),
                            secret.getBytes(), iteration, keyLength)).getEncoded();
            return Base64.getEncoder().encodeToString(result);
        }catch (NoSuchAlgorithmException | InvalidKeySpecException e){
            throw new RuntimeException(e);
        }

    }

    @Override
    public boolean matches(@Nullable CharSequence rawPassword, @Nullable String encodedPassword) {
        return encode(rawPassword).equals(encodedPassword);
    }
}
