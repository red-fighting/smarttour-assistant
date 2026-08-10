package com.panduoma.trevaljava.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {
    @Value("${jwt.secret:defaultSecretKey1234567890abcdefghijklmnopqrstuvwxyz}")
    private String secret="";

    @Value("${jwt.expiration:86400000}")    // 默认24小时
    private long expiration;

    private SecretKey getSecretKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //生成Token
    public String generateToken(Long userId,String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    //解析token
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())//传入和签发 Token 完全一样的加密密钥，用来校验 Token 签名是否合法。
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //验证token是否有效
    public boolean validateToken(String token) {
        try{
            parseToken(token);
            return true;
        }catch (Exception e){
            return false;
        }
    }
    public Long getUserIdFromToken(String token) {
        return parseToken(token).get("userId", Long.class);
    }
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject();
    }
}
