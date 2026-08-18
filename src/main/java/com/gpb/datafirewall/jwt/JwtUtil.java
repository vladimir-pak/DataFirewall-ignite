package com.gpb.datafirewall.jwt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gpb.datafirewall.cef.SvoiLogger;
import com.gpb.datafirewall.cef.enums.SvoiSeverityEnum;
import com.gpb.datafirewall.properties.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {

    private static final String SUBJECT = "datafirewall-spring";
    private static final String SERVICE_CLAIM = "service";

    private final JwtProperties jwtProperties;
    private final JwtTokenRegistryRepository tokenRegistryRepository;
    private final SvoiLogger svoiCustomLogger;

    private final SecretKeySpec signingKey;

    public JwtUtil(
            JwtProperties jwtProperties,
            JwtTokenRegistryRepository tokenRegistryRepository,
            SvoiLogger svoiCustomLogger
    ) {
        this.jwtProperties = jwtProperties;
        this.tokenRegistryRepository = tokenRegistryRepository;
        this.svoiCustomLogger = svoiCustomLogger;

        this.signingKey = new SecretKeySpec(
                jwtProperties.getSecret()
                        .getBytes(StandardCharsets.UTF_8),
                SignatureAlgorithm.HS256.getJcaName()
        );
    }

    @Transactional
    public String generateToken(String secret, String service) {

        validateSecret(secret);

        if (StringUtils.isBlank(service)) {
            throw new IllegalArgumentException(
                    "Service must not be empty"
            );
        }

        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(
                jwtProperties.getExpirationHours(),
                ChronoUnit.HOURS
        );

        UUID jti = UUID.randomUUID();

        String generatedToken = Jwts.builder()
                .setSubject(SUBJECT)
                .setId(jti.toString())
                .setIssuedAt(Date.from(issuedAt))
                .setExpiration(Date.from(expiresAt))
                .claim(SERVICE_CLAIM, service)
                .signWith(
                        SignatureAlgorithm.HS256,
                        signingKey
                )
                .compact();

        tokenRegistryRepository.register(
                jti,
                service,
                SUBJECT,
                issuedAt,
                expiresAt
        );

        svoiCustomLogger.sendInternal(
                "jwtGenerate",
                "Jwt Generation",
                "Jwt has been generated for service: " + service,
                SvoiSeverityEnum.ONE
        );

        return generatedToken;
    }

    public boolean validateToken(String token) {

        try {
            Claims claims = parseClaims(token);

            if (!SUBJECT.equals(claims.getSubject())) {
                return false;
            }

            if (StringUtils.isBlank(claims.getId())) {
                return false;
            }

            if (claims.getIssuedAt() == null) {
                return false;
            }

            if (claims.getExpiration() == null) {
                return false;
            }

            String service = claims.get(
                    SERVICE_CLAIM,
                    String.class
            );

            if (StringUtils.isBlank(service)) {
                return false;
            }

            UUID jti = UUID.fromString(claims.getId());

            return tokenRegistryRepository.isActive(
                    jti,
                    service
            );

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    @Transactional
    public boolean revokeToken(String token) {

        try {
            Claims claims = parseClaims(token);

            UUID jti = UUID.fromString(claims.getId());

            boolean revoked =
                    tokenRegistryRepository.revoke(jti);

            if (revoked) {

                String service = claims.get(
                        SERVICE_CLAIM,
                        String.class
                );

                svoiCustomLogger.sendInternal(
                        "jwtRevoke",
                        "Jwt Revocation",
                        "Jwt has been revoked for service: " + service,
                        SvoiSeverityEnum.ONE
                );
            }

            return revoked;

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {

        return Jwts.parser()
                .setSigningKey(signingKey)
                .parseClaimsJws(token)
                .getBody();
    }

    private void validateSecret(String secret) {

        if (secret == null) {
            throw new IllegalStateException(
                    "Wrong secret"
            );
        }

        boolean valid = MessageDigest.isEqual(
                secret.getBytes(StandardCharsets.UTF_8),
                jwtProperties.getSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );

        if (!valid) {
            throw new IllegalStateException(
                    "Wrong secret"
            );
        }
    }
}