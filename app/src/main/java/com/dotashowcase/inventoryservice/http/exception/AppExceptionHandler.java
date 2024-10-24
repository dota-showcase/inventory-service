package com.dotashowcase.inventoryservice.http.exception;

import com.dotashowcase.inventoryservice.http.exception.response.ErrorResponse;
import com.dotashowcase.inventoryservice.http.exception.response.SteamErrorResponse;
import com.dotashowcase.inventoryservice.http.exception.response.ValidationErrorResponse;
import com.dotashowcase.inventoryservice.http.ratelimiter.RateLimitHandler;
import com.dotashowcase.inventoryservice.http.ratelimiter.RateLimiterException;
import com.dotashowcase.inventoryservice.steamclient.exception.BadRequestException;
import com.dotashowcase.inventoryservice.steamclient.exception.InventoryStatusException;
import com.dotashowcase.inventoryservice.steamclient.exception.SteamException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@ControllerAdvice
public class AppExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AppExceptionHandler.class);

    private static final String LOG_MESSAGE_TEMPLATE = "Body '{}'";

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

        final ErrorResponse errorResponse = getValidationErrorResponse(
                ((ServletWebRequest) request).getRequest().getRequestURI(),
                ex.getName() + ": " + cause.getMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex, WebRequest request) {

        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            // get param name from path
            String paramName = String.valueOf(StreamSupport
                    .stream(violation.getPropertyPath().spliterator(), false)
                    .reduce((first, second) -> second)
                    .orElse(null));

            errors.add(paramName + ": " + violation.getMessage());
        }

        final ErrorResponse errorResponse = getValidationErrorResponse(
                ((ServletWebRequest) request).getRequest().getRequestURI(),
                "Validation failed",
                errors
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(SteamException.class)
    @ResponseBody
    public SteamErrorResponse handleSteamException(final SteamException ex, HttpServletRequest request) {
        SteamErrorResponse.SteamErrorDetail steamErrorDetail = new SteamErrorResponse.SteamErrorDetail();

        HttpStatus steamHttpStatus = HttpStatus.BAD_REQUEST;
        if (ex instanceof BadRequestException) {
            HttpStatus resolvedHttpStatus = HttpStatus.resolve(((BadRequestException) ex).getSteamHttpStatusCode());
            steamHttpStatus = resolvedHttpStatus != null ? resolvedHttpStatus : HttpStatus.BAD_REQUEST;

            steamErrorDetail.setType("Steam Server Error");
            steamErrorDetail.setHttpStatus(steamHttpStatus.value());
            steamErrorDetail.setError(steamHttpStatus.getReasonPhrase());
            steamErrorDetail.setMessage(((BadRequestException) ex).getSteamHttpMessage());
        } else if (ex instanceof InventoryStatusException) {

            steamErrorDetail.setType("Inventory Error");
            steamErrorDetail.setHttpStatus(((InventoryStatusException) ex).getSteamHttpStatusCode());
            steamErrorDetail.setInventoryStatus(((InventoryStatusException) ex).getSteamInventoryStatusCode());
            steamErrorDetail.setError(((InventoryStatusException) ex).getSteamInnerMessage());
            steamErrorDetail.setMessage("Steam responded with a description of the inventory request error");
        }

        final SteamErrorResponse steamErrorResponse = getSteamErrorResponse(
                request.getRequestURI(),
                steamHttpStatus,
                ex.getMessage(),
                steamErrorDetail
        );

        log.error(LOG_MESSAGE_TEMPLATE, steamErrorResponse);

        return steamErrorResponse;
    }

    @ExceptionHandler({RateLimiterException.class})
    public ResponseEntity<ErrorResponse> handleAllExceptions(RateLimiterException ex, HttpServletRequest request) {
        final ErrorResponse errorResponse = getErrorResponse(
                request.getRequestURI(),
                HttpStatus.TOO_MANY_REQUESTS,
                ex.getMessage()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add(RateLimitHandler.HEADER_RETRY_AFTER, String.valueOf(ex.getWaitForRefill()));

        return new ResponseEntity<>(errorResponse, headers, HttpStatus.TOO_MANY_REQUESTS);
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

        ErrorResponse errorResponse = getErrorResponse(request.getRequestURI(), status, message);

        log.error(LOG_MESSAGE_TEMPLATE, errorResponse);

        if (status.value() >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            log.error(sw.toString());
        }

        return new ResponseEntity<>(errorResponse, status);
    }

    private ErrorResponse getErrorResponse(final String requestURI,
                                           final HttpStatus status,
                                           final String message) {
        return new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                requestURI
        );
    }

    private ValidationErrorResponse getValidationErrorResponse(final String requestURI, final String message) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;

        return new ValidationErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                requestURI
        );
    }

    private ValidationErrorResponse getValidationErrorResponse(final String requestURI,
                                                               final String message,
                                                               List<String> errors) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;

        return new ValidationErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                requestURI,
                errors
        );
    }

    private SteamErrorResponse getSteamErrorResponse(final String requestURI,
                                                     final HttpStatus status,
                                                     final String message,
                                                     SteamErrorResponse.SteamErrorDetail steamErrorDetail) {
        return new SteamErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                requestURI,
                steamErrorDetail
        );
    }
}
