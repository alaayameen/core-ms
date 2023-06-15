package com.social.core.gateway.interceptor;

import com.google.gson.Gson;
import com.social.core.models.RuleEnum;
import com.social.core.models.UserInfo;
import com.social.core.rest.exceptions.SimpleClientException;
import com.social.core.utils.JWTUtil;
import com.social.core.utils.JwtProps;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.HttpRetryException;
import java.nio.charset.Charset;

@Slf4j
@AllArgsConstructor
public class RequestResponseLoggingInterceptor implements ClientHttpRequestInterceptor {

    private JWTUtil jwtUtil;
    private JwtProps jwtProps;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException
    {
        logRequest(request, body);
        request.getHeaders().add("Trx-Id" , MDC.get("Trx-Id"));

        if(checkIfPathForAdminOnly(request.getURI().getPath())) {
            UserInfo userInfo = null;
            String token;
            if(!CollectionUtils.isEmpty(request.getHeaders().get("Authorization"))){
                token = request.getHeaders().get("Authorization").get(0).substring(7);
                userInfo = jwtUtil.getUserInfoFromSpasticToken(token);

            }
            if(userInfo == null){
                token = "Bearer " + jwtUtil.generateTokenForAdminUser();
            }else{
                userInfo.setRule(RuleEnum.ADMIN);
                token = "Bearer " + jwtUtil.generateTokenFromUserInfo(userInfo);
            }
            if (request.getHeaders().get("Authorization") != null) {
                request.getHeaders().set("Authorization", token);
            } else {
                request.getHeaders().set("Authorization", token);
            }
        }
        ClientHttpResponse response = execution.execute(request, body);
        logResponse(response);
        if(response != null && !response.getStatusCode().is2xxSuccessful()) {
            handleError(response);
        }
        return response;
    }

    private void handleError(ClientHttpResponse response)  throws IOException {
        String responseBody = null;

        try {
            responseBody = StreamUtils.copyToString(response.getBody(), Charset.defaultCharset());
        }catch (HttpRetryException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED");
        }

        if(responseBody != null){
            SimpleClientException simpleClientException = (new Gson()).fromJson(responseBody, SimpleClientException.class);
            if(simpleClientException != null){
               throw simpleClientException;
            }
        }
    }

    private void logRequest(HttpRequest request, byte[] body) throws IOException
    {
        if (log.isDebugEnabled())
        {
            log.debug("===========================request begin================================================");
            log.debug("URI         : {}", request.getURI());
            log.debug("Method      : {}", request.getMethod());
            log.debug("Headers     : {}", request.getHeaders());
            log.debug("Request body: {}", new String(body, "UTF-8"));
            log.debug("==========================request end================================================");
        }
    }

    private void logResponse(ClientHttpResponse response) throws IOException
    {
        if (log.isDebugEnabled())
        {
            log.debug("============================response begin==========================================");
            log.debug("Status code  : {}", response.getStatusCode());
            log.debug("Status text  : {}", response.getStatusText());
            log.debug("Headers      : {}", response.getHeaders());
            try {
                log.debug("Response body: {}", StreamUtils.copyToString(response.getBody(), Charset.defaultCharset()));
            }catch (Exception e){
                log.debug("Response body: {}", "Unauthorized");
            }
            log.debug("=======================response end=================================================");
        }
    }

    private boolean  checkIfPathForAdminOnly(String path){
        if(jwtProps.getAdminList() == null){
            return false;
        }
        if(jwtProps.getAdminList().contains(path)){
            return true;
        }
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        for(String adminPath : jwtProps.getAdminList()){
            if (antPathMatcher.match(adminPath,path)) {
                return true;
            }
        }
        return false;
    }
}