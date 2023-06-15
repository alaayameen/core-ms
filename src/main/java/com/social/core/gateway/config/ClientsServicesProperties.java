package com.social.core.gateway.config;

import com.social.core.gateway.config.mudels.ClientCredential;
import com.social.core.gateway.config.mudels.Cloudmap;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "aws")
public class ClientsServicesProperties {
    private Cloudmap cloudmap;
    private Map<String, ClientCredential> clients;
}