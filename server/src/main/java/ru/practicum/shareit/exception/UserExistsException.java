package ru.practicum.shareit.exception;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserExistsException extends IllegalArgumentException {
    public UserExistsException(String message) {
        super(message);
        log.error(message);
    }
}