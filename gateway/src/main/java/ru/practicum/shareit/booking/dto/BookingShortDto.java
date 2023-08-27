package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
public class BookingShortDto {
    private Long id;
    private Long bookerId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}

