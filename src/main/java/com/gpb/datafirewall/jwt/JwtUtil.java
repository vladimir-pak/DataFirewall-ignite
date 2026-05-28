package com.gpb.datafirewall.jwt;

import java.time.Duration;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.gpb.datafirewall.cef.SvoiLogger;
import com.gpb.datafirewall.cef.enums.SvoiSeverityEnum;
import com.gpb.datafirewall.properties.JwtProperties;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtProperties jwtProperties;
    private final SvoiLogger svoiCustomLogger;

    public String generateToken(String secret) {
        if (StringUtils.equals(secret, jwtProperties.getSecret())) {
            SecretKeySpec signingKey = new SecretKeySpec(jwtProperties.getSecret().getBytes(), SignatureAlgorithm.HS256.getJcaName());
            String generatedToken = Jwts.builder()
                    .setSubject("datafirewall-spring")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + Duration.ofDays(365 * 10).toMillis()))
                    .signWith(SignatureAlgorithm.HS256, signingKey)
                    .compact();
            svoiCustomLogger.sendInternal(
                "jwtGenerate",
                "Jwt Generation",
                "Jwt has been generated",
                SvoiSeverityEnum.ONE
            );
            return generatedToken;
        } else
            throw new IllegalStateException("Wrong secret");
    }

    public boolean validateToken(String token) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(jwtProperties.getSecret().getBytes(), SignatureAlgorithm.HS256.getJcaName());
            Jwts.parser()
                    .setSigningKey(signingKey)
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
