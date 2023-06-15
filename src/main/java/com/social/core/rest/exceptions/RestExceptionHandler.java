package com.social.core.rest.exceptions;

import com.social.core.models.UserInfo;
import com.social.core.utils.JWTUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
@Log4j2
public class RestExceptionHandler extends DefaultHandlerExceptionResolver {

    @Autowired
    private JWTUtil jwtUtil;

    @ExceptionHandler(ResponseStatusException.class)
    protected ResponseEntity<Object> handleEntityNotFound(HttpServletRequest request, HttpServletResponse response, ResponseStatusException ex) {
        return buildResponseEntity(request, response, ex);
    }

    @ExceptionHandler(SimpleClientException.class)
    protected ResponseEntity<Object> handleEntityNotFound(HttpServletRequest request, HttpServletResponse response, SimpleClientException ex) {
        return buildResponseEntity(response, ex);
    }

    private ResponseEntity<Object> buildResponseEntity(HttpServletRequest request, HttpServletResponse response, ResponseStatusException responseStatusException) {
        try {
            UserInfo userInfo = jwtUtil.getUserInfoFromToken();
            if (request.getHeader("Accept-Language") != null && !"".equals(request.getHeader("Accept-Language"))) {
                userInfo.setLocale(Locale.forLanguageTag(request.getHeader("Accept-Language")));
            }

            SimpleClientErrorResponse simpleClientErrorResponse = new SimpleClientErrorResponse(
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss")),
                    getMessageForLocale(responseStatusException.getReason(), userInfo.getLocale()),
                    responseStatusException.getReason(),
                    responseStatusException.getStatus(),
                    new URL(request.getRequestURL().toString()).getFile(),
                    response.getHeader("Trx-Id")
            );
            log.error(simpleClientErrorResponse);
            return new ResponseEntity<>(simpleClientErrorResponse, responseStatusException.getStatus());
        } catch (MalformedURLException e) {
            return new ResponseEntity<>(responseStatusException, responseStatusException.getStatus());
        }
    }

    private ResponseEntity<Object> buildResponseEntity(HttpServletResponse response, SimpleClientException simpleClientException) {

        SimpleClientErrorResponse simpleClientErrorResponse = new SimpleClientErrorResponse(
                simpleClientException.getTimestamp(),
                simpleClientException.getUserMessage(),
                simpleClientException.getSystemMessage(),
                simpleClientException.getStatus(),
                simpleClientException.getPath(),
                response.getHeader("Trx-Id")
        );
        return new ResponseEntity<>(simpleClientErrorResponse, simpleClientException.getStatus());

    }

    public static String getMessageForLocale(String messageKey, Locale locale) {
        if (locale == null) {
            locale = Locale.forLanguageTag("en");
        }
        ResourceBundle resourceBundle;
        try {
            resourceBundle = ResourceBundle.getBundle("errorMessages", Locale.forLanguageTag(locale.getLanguage()));
        } catch (MissingResourceException e) {
            try {
                Locale.setDefault(Locale.ENGLISH);
                resourceBundle = ResourceBundle.getBundle("errorMessages", Locale.forLanguageTag(locale.getLanguage()));
            } catch (MissingResourceException e2) {
                return messageKey;
            }
        }
        try {
            return new String(resourceBundle.getString(messageKey).getBytes("ISO-8859-1"), "UTF-8");
        } catch (MissingResourceException | UnsupportedEncodingException e) {
            try {
                return new String(resourceBundle.getString("GENERAL_ERROR_MESSAGE").getBytes("ISO-8859-1"), "UTF-8");
            } catch (UnsupportedEncodingException unsupportedEncodingException) {
                unsupportedEncodingException.printStackTrace();
                return resourceBundle.getString("GENERAL_ERROR_MESSAGE");
            }
        }
    }
}