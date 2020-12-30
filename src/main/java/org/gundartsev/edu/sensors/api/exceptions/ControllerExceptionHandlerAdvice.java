package org.gundartsev.edu.sensors.api.exceptions;

import org.gundartsev.edu.sensors.common.exception.SensorNotFoundException;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

/**
 * Advicer to intercept and wrap all abnormal cases and produce proper {@link HttpStatus}
 */

@RestControllerAdvice
public class ControllerExceptionHandlerAdvice {
    @ExceptionHandler
    public final Mono<ResponseEntity<Object>> handleException(ServerWebExchange exchange, Exception exception) {

        if (exception instanceof SensorNotFoundException) {
            return Mono.just(ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(exception.getMessage()));
        }
        if (exception instanceof WebExchangeBindException ||
            exception instanceof ServerWebInputException){
            return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid request data"));
        }
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(exception.getMessage()));
    }
}
