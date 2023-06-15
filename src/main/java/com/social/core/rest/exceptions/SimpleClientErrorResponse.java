package com.social.core.rest.exceptions;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@Data
@ToString
public class SimpleClientErrorResponse{
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private String timestamp;
    private String userMessage;
    private String systemMessage = "TracedException";
    private HttpStatus status;
    private String path;
    private String transactionId;
}