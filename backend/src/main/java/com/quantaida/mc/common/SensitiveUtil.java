package com.quantaida.mc.common;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 敏感字段加密 / 脱敏（手机号、证件号）。
 */
@Component
public class SensitiveUtil {
    private final AES aes;

    public SensitiveUtil(@Value("${app.crypto.key}") String key) {
        byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = new byte[32];
        System.arraycopy(bytes, 0, keyBytes, 0, Math.min(bytes.length, 32));
        this.aes = SecureUtil.aes(keyBytes);
    }

    public String encrypt(String plain) {
        return plain == null ? null : aes.encryptBase64(plain);
    }

    public String decrypt(String cipher) {
        if (cipher == null || cipher.isBlank()) {
            return null;
        }
        try {
            return aes.decryptStr(cipher);
        } catch (Exception e) {
            return null;
        }
    }

    public String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    public String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 4) + "**********" + idCard.substring(idCard.length() - 4);
    }

    public String hash(String value) {
        return value == null ? null : SecureUtil.sha256(value);
    }
}
