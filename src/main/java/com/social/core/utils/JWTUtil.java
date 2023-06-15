package com.social.core.utils;

import com.social.core.models.RuleEnum;
import com.social.core.models.UsedLoginEnum;
import com.social.core.models.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@Component
@Log4j2
@Data
public class JWTUtil {

    private SecretKey secretKey;
    @Autowired
    private JwtProps jwtProps;

    @PostConstruct
    public void setUpSecretKey() {
        try {
            secretKey = Keys.hmacShaKeyFor(jwtProps.getSecret().getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error generating JWT Secret Key : {}", e.getMessage());
            throw new RuntimeException("Error generating JWT Secret Key", e);
        }
    }

    public String generateTokenForAnonymousUser() {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject( "tokenForAnonymousUser")
                .setIssuer(jwtProps.getIssuer())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(
                        Duration.ofSeconds(jwtProps.getTimeToLiveInSeconds()))))
                .claim("mobile", "0000000000")
                .claim("id", "00000")
                .claim("usedLogin", UsedLoginEnum.MOBILE)
                .claim("rule", RuleEnum.ANONYMOUS)
                .signWith(secretKey)
                .compact();
    }

    public String generateTokenForAdminUser() {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject( "tokenForAnonymousUser")
                .setIssuer(jwtProps.getIssuer())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(
                        Duration.ofSeconds(jwtProps.getTimeToLiveInSeconds()))))
                .claim("mobile", "123123123")
                .claim("id", "12345")
                .claim("usedLogin", UsedLoginEnum.MOBILE)
                .claim("rule", RuleEnum.ADMIN)
                .signWith(secretKey)
                .compact();
    }

    public String generateTokenFromUserInfo(UserInfo user) {
        String subject = null;
        if (user.getUsedLoginEnum().equals(UsedLoginEnum.MOBILE)) {
            subject = "mobile:" + user.getMobileNumber();
        } else {
            subject = "email:" + user.getEmail();
        }
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(subject)
                .setIssuer(jwtProps.getIssuer())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(
                        Duration.ofSeconds(jwtProps.getTimeToLiveInSeconds()))))
                .claim("mobile", user.getMobileNumber())
                .claim("email", user.getEmail())
                .claim("id", user.getId())
                .claim("usedLogin", user.getUsedLoginEnum())
                .claim("locale", user.getLocale())
                .claim("rule", user.getRule())
                .claim("isActive", user.getIsActive())
                .claim("isDeleted", user.getIsDeleted())
                .claim("userName", user.getUserName())
                .claim("appUserName", user.getAppUserName())
                .claim("numericUserId", user.getNumericUserId())
                .signWith(secretKey)
                .compact();
    }

    public Claims parseJWT(String jwtString) {

        return
                Jwts.parserBuilder()
                        .setSigningKey(secretKey)
                        .build()
                        .parseClaimsJws(jwtString).getBody();
    }

    public void validateToken(String token) {
        String errorMessage = null;
        try {
            this.parseJWT(token);
        }catch (IllegalArgumentException e) {
            errorMessage = "TOKEN_IS_NOT_VALID";
        } catch (ExpiredJwtException ex) {
            errorMessage = "TOKEN_IS_EXPIRED";
        } catch (Exception ex) {
            errorMessage =  "TOKEN_IS_NOT_VALID";
        }
        if(errorMessage != null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,errorMessage);
        }
    }

    public UserInfo getUserInfoFromToken() {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof UserInfo) {
            return ((UserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        } else {
            return new UserInfo();
        }
    }

    public UserInfo getUserInfoFromSpasticToken(String token){
        Claims claims = parseJWT(token);
        String subject = claims.getSubject();
        UserInfo userInfo = new UserInfo();
        userInfo.setMobileNumber((String) claims.get("mobile"));
        userInfo.setEmail((String) claims.get("email"));
        userInfo.setSubject(subject);
        userInfo.setUsedLoginEnum(UsedLoginEnum.fromValue((String)claims.get("usedLogin")));
        userInfo.setId((String) parseJWT(token).get("id"));
        userInfo.setRule(RuleEnum.fromValue((String)claims.get("rule")));
        userInfo.setUserName((String) claims.get("userName"));
        userInfo.setAppUserName((String) claims.get("appUserName"));
        userInfo.setNumericUserId((Integer) claims.get("numericUserId"));
        
        if(claims.get("locale") != null) {
            userInfo.setLocale((Locale.forLanguageTag((String)claims.get("locale"))) );
        }
        return userInfo;
    }
    
    public boolean isAdminUser() {
		return RuleEnum.SUPER_ADMIN.getValue().contains(getUserInfoFromToken().getRule().getValue());
	}
}