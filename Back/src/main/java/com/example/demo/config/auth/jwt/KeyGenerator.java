package com.example.demo.config.auth.jwt;

import io.jsonwebtoken.SignatureAlgorithm; // HS256 알고리즘 명시
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;

@Slf4j
public class KeyGenerator {

    //HS256 알고리즘을 위한 256비트 이상의 난수 키 생성
    //@return 생성된 키의 바이트 배열

    public static byte[] keyGen() {
        // JWT 표준에 맞춰 HS256 알고리즘에 적합한 강력한 키를 생성
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        // 생성된 키를 Base64로 인코딩하여 로깅
        String base64EncodedKey = Encoders.BASE64.encode(key.getEncoded());
        log.warn("새로 생성된 JWT 서명 키 (Base64 인코딩): {}", base64EncodedKey);

        return key.getEncoded();
    }
}