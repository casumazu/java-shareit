package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestNotFoundException extends IllegalArgumentException {
    public RequestNotFoundException(String message) {
        super(message);
        log.error(message);
    }
}
