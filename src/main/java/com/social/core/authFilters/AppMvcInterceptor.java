package com.social.core.authFilters;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class AppMvcInterceptor implements HandlerInterceptor {
    
    private String TRACE_HEADER = "Trx-Id";
    
    public AppMvcInterceptor() {
        MDC.put("Trx-Id", UUID.randomUUID().toString());
    }



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String traceID = response.getHeader(TRACE_HEADER);

        if(traceID == null){
            if (request.getHeader(TRACE_HEADER) != null && !request.getHeader(TRACE_HEADER).isEmpty()) {
                traceID = request.getHeader(TRACE_HEADER);
            }else{
                traceID = UUID.randomUUID().toString().replace("-","");
            }
            response.setHeader("Trx-Id", traceID);
        }
        MDC.put("Trx-Id", traceID);
        return true;
    }
}