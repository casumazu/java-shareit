package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInputDto;

import ru.practicum.shareit.exception.BookingNotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookingServiceImpl bookingService;
    private final long id = 1L;
    private final LocalDateTime start = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusHours(1);
    private final LocalDateTime end = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).plusDays(1);
    private final BookingInputDto bookingInputDto = new BookingInputDto(1L, start, end);
    private final UserDto bookerDto = new UserDto(1L, "booker", "booker@yandex.ru");
    private final ItemDto itemDto = new ItemDto(
            1L, "item", "description", true, null, null);
    private final BookingDto bookingDto = new BookingDto(id, start, end, itemDto, bookerDto, Status.WAITING);

    @SneakyThrows
    @Test
    void addBooking() {
        when(bookingService.add(1L, bookingInputDto)).thenReturn(bookingDto);

        String result = mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingInputDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertEquals(objectMapper.writeValueAsString(bookingDto), result);
        verify(bookingService).add(1L, bookingInputDto);
    }

    @SneakyThrows
    @Test
    void addBooking_whenBookingInputDtoDateTimeNotValid_thenValidationException() {
        when(bookingService.add(1L, bookingInputDto)).thenThrow(ValidationException.class);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingInputDto)))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void addBooking_whenBookerIdEqualsItemOwnerId_thenBookingNotFoundException() {
        when(bookingService.add(1L, bookingInputDto)).thenThrow(BookingNotFoundException.class);

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(bookingInputDto)))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    void confirmationBooking() {
        bookingDto.setStatus(Status.APPROVED);
        when(bookingService.approve(2L, id, true)).thenReturn(bookingDto);

        mvc.perform(patch("/bookings/{bookingId}", id)
                        .header("X-Sharer-User-Id", 2)
                        .queryParam("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(bookingDto.getStatus().toString())))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().toString())))
                .andExpect(jsonPath("$.item.id", is(bookingDto.getItem().getId().intValue())));

        verify(bookingService).approve(2L, id, true);
    }

    @SneakyThrows
    @Test
    void findById() {
        when(bookingService.getBookingById(1L, id)).thenReturn(bookingDto);

        mvc.perform(get("/bookings/{bookingId}", id)
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().toString())))
                .andExpect(jsonPath("$.item.id", is(bookingDto.getItem().getId().intValue())));

        verify(bookingService).getBookingById(1L, id);
    }

    @SneakyThrows
    @Test
    void findBookingsByOwner() {
        List<BookingDto> expected = List.of(bookingDto);
        when(bookingService.getAllBookingsForOwner(2L, "ALL", 0, 20)).thenReturn(expected);

        String result = mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 2)
                        .queryParam("from", "0")
                        .queryParam("size", "20")
                        .queryParam("state", "ALL"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        verify(bookingService).getAllBookingsForOwner(2L, "ALL", 0, 20);
        assertThat(objectMapper.writeValueAsString(expected), equalTo(result));
    }

    @SneakyThrows
    @Test
    void findBookingsByOwner_whenStateIsNotValid_thenUnknownStateException() {
        when(bookingService.getAllBookingsForOwner(2L, "DESC", 0, 20))
                .thenThrow(ValidationException.class);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 2)
                        .queryParam("from", "0")
                        .queryParam("size", "20")
                        .queryParam("state", "DESC"))
                .andExpect(status().isBadRequest());
    }
}