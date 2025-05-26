package com.example.courseplatform.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class CustomErrorController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(CustomErrorController.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        // Get error details
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object requestUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        // Default values
        String errorMessage = "An unexpected error occurred";
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        String path = request.getRequestURI();
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            httpStatus = HttpStatus.valueOf(statusCode);
        }
        
        if (message != null && !message.toString().isEmpty()) {
            errorMessage = message.toString();
        } else if (exception != null) {
            // Get the root cause exception
            Throwable rootCause = getRootCause((Throwable) exception);
            errorMessage = rootCause.getMessage();
            logger.error("Error occurred: ", rootCause);
        }
        
        if (requestUri != null) {
            path = requestUri.toString();
        }
        
        // Add attributes to the model
        model.addAttribute("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        model.addAttribute("status", httpStatus.value());
        model.addAttribute("error", httpStatus.getReasonPhrase());
        model.addAttribute("message", errorMessage);
        model.addAttribute("path", path);
        
        // Check for API request
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("application/json")) {
            return "error/error-json";
        }
        
        return "error/error";
    }
    
    // Helper method to get the root cause of an exception
    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause != null) {
            return getRootCause(cause);
        }
        return throwable;
    }
} 