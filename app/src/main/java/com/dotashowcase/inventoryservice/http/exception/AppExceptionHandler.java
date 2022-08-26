package com.dotashowcase.inventoryservice.http.exception;

import com.dotashowcase.inventoryservice.steamclient.exception.BadRequestException;
import com.dotashowcase.inventoryservice.steamclient.exception.InventoryStatusException;
import com.dotashowcase.inventoryservice.steamclient.exception.SteamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

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

    @ExceptionHandler(SteamException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, Object> handleSteamException(final SteamException ex, HttpServletRequest request) {
        final Map<String, Object> body = getExceptionBody(request, HttpStatus.BAD_REQUEST, ex.getMessage());

        final Map<String, Object> steamBody = new LinkedHashMap<>();

        if (ex instanceof BadRequestException) {
            HttpStatus resolvedHttpStatus = HttpStatus.resolve(((BadRequestException) ex).getSteamHttpStatusCode());
            HttpStatus steamHttpStatus = resolvedHttpStatus != null ? resolvedHttpStatus : HttpStatus.BAD_REQUEST;

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

        body.put("steam", steamBody);

        log.error(LOG_MESSAGE_TEMPLATE, body);

        return body;
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

        Map<String, Object> body = getExceptionBody(request, status, message);

        log.error(LOG_MESSAGE_TEMPLATE, body);

        if (status.value() >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);

            log.error(sw.toString());
        }

        return new ResponseEntity<>(body, status);
    }

    private Map<String, Object> getExceptionBody(final HttpServletRequest request,
                                                 final HttpStatus status,
                                                 final String message) {
        final Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getRequestURI());

        return body;
    }
}
