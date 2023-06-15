package com.social.core.gateway.config.mudels;

import lombok.Data;

@Data
public class ClientCredential {

        private String fullHost;
        private String serviceName;
        private int port;
        private String protocol;

    }