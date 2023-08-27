package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.service.BookingService;


import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequestMapping("/bookings")
public class BookingController {

    private static final String OWNER = "X-Sharer-User-Id";
    private final BookingService service;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.service = bookingService;
    }

    @ResponseBody
    @PostMapping
    public BookingDto create(@Valid @RequestBody BookingInputDto bookingInputDto,
                             @RequestHeader(OWNER) Long bookerId) {
        log.info("Получен POST-запрос '/bookings' на создание бронирования от пользователя с ID={}", bookerId);
        return service.add(bookerId, bookingInputDto);
    }

    @ResponseBody
    @PatchMapping("/{bookingId}")
    public BookingDto update(@RequestHeader(OWNER) Long userId,
                             @PathVariable Long bookingId,
                             @RequestParam Boolean approved) {
        log.info("Получен PATCH-запрос '/bookings' на обновление статуса бронирования с ID={}", bookingId);
        return service.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@PathVariable Long bookingId, @RequestHeader(OWNER) Long userId) {
        log.info("Получен GET-запрос '/bookings' на получение бронирования с ID={}", bookingId);
        return service.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getBookings(@RequestParam(name = "state", defaultValue = "ALL") String state,
                                        @RequestHeader(OWNER) Long userId,
                                        @Valid @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                        @Valid @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        log.info("Получен GET-запрос '/bookings' на получение списка всех бронирований " +
                "пользователя с ID={} с параметром STATE={}", userId, state);
        return service.getAllBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsOwner(@RequestParam(name = "state", defaultValue = "ALL") String state,
                                             @RequestHeader(OWNER) Long userId,
                                             @Valid @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
                                             @Valid @RequestParam(value = "size", defaultValue = "20") @Min(1) @Max(100) Integer size) {
        log.info("Получен GET-запрос '/bookings/owner' на получение списка всех бронирований " +
                "вещей пользователя с ID={} с параметром STATE={}", userId, state);
        return service.getAllBookingsForOwner(userId, state, from, size);
    }
}
