package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingInputDto;
import ru.practicum.shareit.booking.dto.BookingState;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    public static final String HEADER_USER_ID = "X-Sharer-User-Id";
    private final BookingClient bookingClient;

    @GetMapping
    public ResponseEntity<Object> findAllBookings(@RequestHeader(HEADER_USER_ID) long userId,
                                              @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                                  @Valid @RequestParam(value = "from", defaultValue = "0")
                                                      @Min(0) Integer from,
                                                  @Valid @RequestParam(value = "size", defaultValue = "20")
                                                      @Min(1) @Max(100) Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Получен GET запрос на просмотр {} бронирований, отправлен пользователем id {}", state, userId);
        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> findBookingsByOwner(@RequestHeader(HEADER_USER_ID) long userId,
                                                  @RequestParam(name = "state", defaultValue = "all") String stateParam,
                                                      @Valid @RequestParam(value = "from", defaultValue = "0")
                                                          @Min(0) Integer from,
                                                      @Valid @RequestParam(value = "size", defaultValue = "20")
                                                          @Min(1) @Max(100) Integer size) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Получен GET запрос на просмотр {} бронирований всех вещей, отправлен пользователем id {}",
                state, userId);
        return bookingClient.findBookingsByOwner(userId, state, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader(HEADER_USER_ID) long userId,
                                           @RequestBody @Valid BookingInputDto requestDto) {
        log.info("Получен запрос POST на бронирование {}, от пользователя id {}", requestDto, userId);
        return bookingClient.bookItem(userId, requestDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> findById(@RequestHeader(HEADER_USER_ID) long userId,
                                             @PathVariable Long bookingId) {
        log.info("Получен GET запрос на просмотр бронирования id {}, отправлен пользователем id {}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> update(@RequestHeader(HEADER_USER_ID) long userId,
                                   @PathVariable Long bookingId,
                                   @RequestParam Boolean approved) {
        log.info("Получен запрос PATCH обновление статуса бронирования {}, от пользователя id {}, статус {}",
                bookingId, userId, approved);
        return bookingClient.update(userId, bookingId, approved);
    }
}