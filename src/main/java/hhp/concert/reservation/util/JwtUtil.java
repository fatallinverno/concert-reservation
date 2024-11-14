package hhp.concert.reservation.util;

import hhp.concert.reservation.domain.entity.TokenEntity;
import hhp.concert.reservation.infrastructure.repository.TokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    @Autowired
    private final TokenRepository tokenRepository;

    public JwtUtil(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public String generateToken(Long userId, int queuePosition) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("queuePosition", queuePosition);
        claims.put("status", "pending");

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))  // 토큰 생성 시간
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24시간 후 만료
                .signWith(secretKey, SignatureAlgorithm.HS256) // 생성된 비밀 키와 HS256 알고리즘으로 서명
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public boolean validateToken(String token, Long userId) {
        Claims claims = extractClaims(token);
        Long tokenUserId = claims.get("userId", Long.class);

        if(isTokenExpired(token)) {
            throw new IllegalArgumentException("토큰이 만료 되었습니다.");
        }

        TokenEntity tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 토큰입니다."));

        if ("COMPLETE".equals(tokenEntity.getStatus())) {
            throw new IllegalArgumentException("토큰이 이미 사용되었습니다.");
        }

        return (userId.equals(tokenUserId)) && !isTokenExpired(token);
    }

}
