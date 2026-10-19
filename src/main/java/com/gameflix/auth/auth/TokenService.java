package com.gameflix.auth.auth;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import com.gameflix.auth.user.User;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final long lifetimeMinutes;

    public TokenService(
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.lifetime-minutes:60}") long lifetimeMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.lifetimeMinutes = lifetimeMinutes;
    }

    public LoginResponse createToken(User user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(lifetimeMinutes * 60);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("gameflix-user-service")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder
                .encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();

        return new LoginResponse(token, "Bearer", lifetimeMinutes * 60);
    }
}
