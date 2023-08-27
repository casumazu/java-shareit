package ru.practicum.shareit.booking.dto;

import java.util.Optional;

public enum Status {
    ALL,

    CURRENT,

    FUTURE,

    PAST,
    REJECTED,
    WAITING;

    public static Optional<Status> from(String stringState) {
        for (Status state : values()) {
            if (state.name().equalsIgnoreCase(stringState)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}