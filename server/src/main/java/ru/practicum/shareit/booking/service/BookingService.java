package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;

import java.util.List;

public interface BookingService {
    BookingDto add(Long userId, BookingInputDto bookingRequestDto);

    BookingDto approve(Long userId, Long bookingId, Boolean isApproved);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getAllBookings(Long userId, String state, Integer from, Integer size);

    List<BookingDto> getAllBookingsForOwner(Long userId, String state, Integer from, Integer size);

}
