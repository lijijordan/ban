package com.jordan.ban.common;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Util {

    private static final String HMACSHA256 = "HmacSHA256";
    private static final String UTF8 = "UTF-8";

    public static String encodeByHmacSHA256(String key, String data) throws Exception {
        Mac sha256_HMAC = Mac.getInstance(HMACSHA256);
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(UTF8), HMACSHA256);
        sha256_HMAC.init(secret_key);
        return Base64.encodeBase64String(sha256_HMAC.doFinal(data.getBytes(UTF8)));
    }

    public static void main(String[] args) throws Exception {
        System.out.println(encodeByHmacSHA256("key", "The quick brown fox jumps over the lazy dog"));
    }
}
