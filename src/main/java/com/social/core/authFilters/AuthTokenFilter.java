package com.social.core.authFilters;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.social.core.utils.JWTUtil;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.social.core.models.RuleEnum;
import com.social.core.models.UsedLoginEnum;
import com.social.core.models.UserInfo;
import com.social.core.utils.JwtProps;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class AuthTokenFilter extends OncePerRequestFilter {

    public static final String _BEARER = "Bearer ";

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private JwtProps jwtProps;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = new URL(request.getRequestURL().toString()).getPath();
        String errorMessage = null;

        String headerAuth = request.getHeader(HttpHeaders.AUTHORIZATION);
        String agent = request.getHeader("User-Agent");
        
       log.debug("REQUEST path {} AGENT {}",path,agent);
        
        if(headerAuth!= null ||
                (!request.getMethod().equals("OPTIONS")
                        && !checkIsInExcludedList(path)
                        && !path.contains("/healthy")
                        && !path.contains(".well-known"))) {
            try {

                if (StringUtils.hasText(headerAuth) && headerAuth.startsWith(_BEARER)) {
                    String jwtToken = headerAuth.substring(7);
                    UserInfo userInfo = new UserInfo();
                    Claims claims = jwtUtil.parseJWT(jwtToken);

                    String subject = claims.getSubject();
                    userInfo.setMobileNumber((String) claims.get("mobile"));
                    userInfo.setEmail((String)claims.get("email"));
                    userInfo.setSubject(subject);
                    userInfo.setUsedLoginEnum(UsedLoginEnum.fromValue((String)claims.get("usedLogin")));
                    userInfo.setId((String)claims.get("id"));
                    userInfo.setRule(RuleEnum.fromValue((String)claims.get("rule")));
                    userInfo.setIsDeleted((Boolean) claims.get("isDeleted"));
                    userInfo.setIsActive((Boolean) claims.get("isActive"));
                    userInfo.setAppUserName((String)claims.get("appUserName"));
                    userInfo.setNumericUserId((Integer) claims.get("numericUserId"));
                    
                    
                    if (userInfo.getIsActive() != null && Boolean.FALSE.equals(userInfo.getIsActive())) {
                        errorMessage =  "INACTIVE_USER";
                        log.error("INACTIVE_USER");
                    }
                    if (userInfo.getIsDeleted() != null && Boolean.TRUE.equals(userInfo.getIsDeleted())) {
                        errorMessage =  "MARKED_FOR_DELETE_USER";
                        log.error("MARKED_FOR_DELETE_USER");
                    }
                    if(checkIfPathForAdminOnly(path) && (userInfo.getRule()== null || !RuleEnum.SUPER_ADMIN.getValue().contains(userInfo.getRule().getValue()))){
                        errorMessage =  "NOT_ADMIN_USER";
                        log.error("NOT_ADMIN_USER");
                    }
                    if(claims.get("locale") != null) {
                        userInfo.setLocale((Locale.forLanguageTag((String)claims.get("locale"))) );
                    }
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userInfo, null);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else{
                    errorMessage =  "TOKEN_IS_REQUIRED";
                }

            } catch (IllegalArgumentException e) {
                log.error("an error occured during getting username from token", e);
                errorMessage = "TOKEN_IS_NOT_VALID";
            } catch (ExpiredJwtException ex) {
                errorMessage = "TOKEN_IS_EXPIRED";
            } catch (Exception ex) {
                log.error("Error authenticating user request : {}", ex.getMessage());
                errorMessage =  "TOKEN_IS_NOT_VALID";
            }
        }


        if(errorMessage != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("{ ");
            sb.append("\"error\": \"Unauthorized\" ,");
            sb.append("\"status\": 401,");
            sb.append("\"message\":\""+ errorMessage +"\",");
            sb.append("\"path\":\"")
                    .append(request.getRequestURL());
            sb.append("\"} ");

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(sb.toString());
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean checkIsInExcludedList(String path){
        if(jwtProps.getExcludedList().contains(path)){
            return true;
        }
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        for(String excludePath : jwtProps.getExcludedList()){
            if (antPathMatcher.match(excludePath,path)) {
                return true;
            }
        }
        return false;
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