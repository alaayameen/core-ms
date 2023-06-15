package com.social.core.rest.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Data
@EqualsAndHashCode
public class SimpleClientException extends RuntimeException {
    private String timestamp;
    private String userMessage;
    private String systemMessage = "TracedException";
    private HttpStatus status;
    private String path;
    private String transactionId;
}
