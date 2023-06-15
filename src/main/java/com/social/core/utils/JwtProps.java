package com.social.core.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "auth.jwt")
@Data
public class JwtProps {
    private String issuer;
    private String secret;
    private String audience;
    private long timeToLiveInSeconds;
    private List<String> excludedList;
    private List<String> adminList;
}