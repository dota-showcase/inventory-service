package com.dotashowcase.inventoryservice.http.exception;

import com.dotashowcase.inventoryservice.http.ratelimiter.RateLimitHandler;
import com.dotashowcase.inventoryservice.http.ratelimiter.RateLimiterException;
import com.dotashowcase.inventoryservice.steamclient.exception.BadRequestException;
import com.dotashowcase.inventoryservice.steamclient.exception.InventoryStatusException;
import com.dotashowcase.inventoryservice.steamclient.exception.SteamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

/**
 * Handles Steam Client exceptions
 */
@ControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AppExceptionHandler.class);

    private static final String LOG_MESSAGE_TEMPLATE = "Body '{}'";

//    @ExceptionHandler(SteamClientException.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ResponseBody
//    public Map<String, Object> handleSteamClientException(final SteamClientException ex, WebRequest request) {
//        return getExceptionBody(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
//    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            WebRequest request
    ) {

        Throwable cause = ex;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }

        final Map<String, Object> body = getExceptionBody(
                ((ServletWebRequest)request).getRequest().getRequestURI(),
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getName() + ": " + cause.getMessage()
        );

        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {
        final Map<String, Object> body = getExceptionBody(
                ((ServletWebRequest)request).getRequest().getRequestURI(),
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Validation failed"
        );

        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            // get param name from path
            String paramName = String.valueOf(StreamSupport
                    .stream(violation.getPropertyPath().spliterator(), false)
                    .reduce((first, second) -> second)
                    .orElse(null));

            errors.add(paramName + ": " + violation.getMessage());
        }

        body.put("validation", errors);

        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(SteamException.class)
    @ResponseBody
    public Map<String, Object> handleSteamException(final SteamException ex, HttpServletRequest request) {
        final Map<String, Object> steamBody = new LinkedHashMap<>();

        HttpStatus steamHttpStatus = HttpStatus.BAD_REQUEST;
        if (ex instanceof BadRequestException) {
            HttpStatus resolvedHttpStatus = HttpStatus.resolve(((BadRequestException) ex).getSteamHttpStatusCode());
            steamHttpStatus = resolvedHttpStatus != null ? resolvedHttpStatus : HttpStatus.BAD_REQUEST;

            steamBody.put("type", "Steam Server Error");
            steamBody.put("status", steamHttpStatus.value());
            steamBody.put("error", steamHttpStatus.getReasonPhrase());
            steamBody.put("message", ((BadRequestException) ex).getSteamHttpMessage());
        } else if (ex instanceof InventoryStatusException) {
            steamBody.put("type", "Inventory Error");
            steamBody.put("status", ((InventoryStatusException) ex).getSteamInnerStatusCode());
            steamBody.put("error", ((InventoryStatusException) ex).getSteamInnerMessage());
            steamBody.put("message", "Steam responded with a description of the inventory request error");
        }

        final Map<String, Object> body = getExceptionBody(
                request.getRequestURI(),
                steamHttpStatus,
                ex.getMessage()
        );

        body.put("steam", steamBody);

        log.error(LOG_MESSAGE_TEMPLATE, body);

        return body;
    }

    @ExceptionHandler({RateLimiterException.class})
    public ResponseEntity<Map<String, Object>> handleAllExceptions(RateLimiterException ex, HttpServletRequest request) {
        final Map<String, Object> body = getExceptionBody(
                request.getRequestURI(),
                HttpStatus.TOO_MANY_REQUESTS,
                ex.getMessage()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add(RateLimitHandler.HEADER_RETRY_AFTER, String.valueOf(ex.getWaitForRefill()));

        return new ResponseEntity<>(body, headers, HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAllExceptions(Exception ex, HttpServletRequest request) {
        ResponseStatus responseStatus = ex.getClass().getAnnotation(ResponseStatus.class);
        HttpStatus status = responseStatus != null
                ? responseStatus.value()
                : HttpStatus.INTERNAL_SERVER_ERROR;

        String message = (responseStatus != null)
                ? responseStatus.reason().length() > 0 ? responseStatus.reason() : ex.getMessage()
                : ex.getMessage();

        Map<String, Object> body = getExceptionBody(request.getRequestURI(), status, message);

        log.error(LOG_MESSAGE_TEMPLATE, body);

        if (status.value() >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            log.error(sw.toString());
        }

        return new ResponseEntity<>(body, status);
    }

    private Map<String, Object> getExceptionBody(final String requestURI,
                                                 final HttpStatus status,
                                                 final String message) {
        final Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", requestURI);

        return body;
    }
}
