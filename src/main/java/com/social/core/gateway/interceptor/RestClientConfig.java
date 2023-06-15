package com.social.core.gateway.interceptor;

import com.social.core.utils.JWTUtil;
import com.social.core.utils.JwtProps;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
@AllArgsConstructor
public class RestClientConfig {

    private JWTUtil jwtUtil;
    private JwtProps jwtProps;
    @Bean
    public RestTemplate restTemplate() {
        final RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new
                SimpleClientHttpRequestFactory()));
        List<ClientHttpRequestInterceptor> interceptors
          = restTemplate.getInterceptors();
        if (CollectionUtils.isEmpty(interceptors)) {
            interceptors = new ArrayList<>();
        }
        interceptors.add(new RequestResponseLoggingInterceptor(jwtUtil,jwtProps));
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }
}